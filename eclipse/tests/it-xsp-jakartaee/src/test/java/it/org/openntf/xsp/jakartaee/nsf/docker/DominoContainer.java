package it.org.openntf.xsp.jakartaee.nsf.docker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

public class DominoContainer extends GenericContainer<DominoContainer> {
	private static final String[] BUNDLE_DEPS = {
		"org.openntf.xsp.test.postinstall", //$NON-NLS-1$
		"org.openntf.xsp.test.beanbundle" //$NON-NLS-1$
	};
	
	public static final Set<Path> tempFiles = new HashSet<>();
	
	public static class DominoImage extends ImageFromDockerfile {
		public DominoImage() {
			super("xsp-jakaetaee-testcontainer:1.0.0", false); //$NON-NLS-1$
			withFileFromClasspath("Dockerfile", "/docker/Dockerfile"); //$NON-NLS-1$ //$NON-NLS-2$
			withFileFromClasspath("domino-config.json", "/docker/domino-config.json"); //$NON-NLS-1$ //$NON-NLS-2$
			withFileFromClasspath("java.policy", "/docker/java.policy"); //$NON-NLS-1$ //$NON-NLS-2$

			// Find the current build version
			Properties props = new Properties();
			try (InputStream is = getClass().getResourceAsStream("/scm.properties")) { //$NON-NLS-1$
				props.load(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			String version = props.getProperty("git.build.version", null); //$NON-NLS-1$
			if(StringUtil.isEmpty(version)) {
				throw new RuntimeException("Unable to determine artifact version from scm.properties");
			}
			
			Path exampleNtf = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaee-example", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			withFileFromPath("notesdata/jakartaee.ntf", exampleNtf); //$NON-NLS-1$
			Path bundleExampleNtf  = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaee-bundle-example", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			withFileFromPath("notesdata/jeebundle.ntf", bundleExampleNtf); //$NON-NLS-1$

			// Build a data.zip with the expected update site and NTF
			Path updateSite = findLocalMavenArtifact("org.openntf.xsp", "org.openntf.xsp.jakartaee.updatesite", version, "zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			try {
				Path dataZip = Files.createTempFile(getClass().getName(), ".zip"); //$NON-NLS-1$
				tempFiles.add(dataZip);
				try(
					OutputStream os = Files.newOutputStream(dataZip, StandardOpenOption.TRUNCATE_EXISTING);
					ZipOutputStream zos = new ZipOutputStream(os, StandardCharsets.UTF_8)
				) {
					
					// Copy in the project update site
					try(
						InputStream is = Files.newInputStream(updateSite);
						ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)
					) {
						for(ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
							String targetPath = PathUtil.concat("domino/workspace/applications/eclipse", entry.getName(), '/'); //$NON-NLS-1$
							zos.putNextEntry(new ZipEntry(targetPath));
							StreamUtil.copyStream(zis, zos);
							zos.closeEntry();
						}
					}
					
					// Copy in the test-support bundles
					for(String bundleName : BUNDLE_DEPS) {
						Path postinstall = findLocalMavenArtifact("org.openntf.xsp", bundleName, version, "jar"); //$NON-NLS-1$ //$NON-NLS-2$
						
						ZipEntry entry = new ZipEntry("domino/workspace/applications/eclipse/plugins/" + bundleName + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
						zos.putNextEntry(entry);
						Files.copy(postinstall, zos);
						zos.closeEntry();
					}
				}
				
				withFileFromPath("data.zip", dataZip); //$NON-NLS-1$
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			
		}

		private Path findLocalMavenArtifact(String groupId, String artifactId, String version, String type) {
			String mavenRepo = System.getProperty("maven.repo.local"); //$NON-NLS-1$
			if (StringUtil.isEmpty(mavenRepo)) {
				mavenRepo = PathUtil.concat(System.getProperty("user.home"), ".m2", File.separatorChar); //$NON-NLS-1$ //$NON-NLS-2$
				mavenRepo = PathUtil.concat(mavenRepo, "repository", File.separatorChar); //$NON-NLS-1$
			}
			String groupPath = groupId.replace('.', File.separatorChar);
			Path localPath = Paths.get(mavenRepo).resolve(groupPath).resolve(artifactId).resolve(version);
			String fileName = StringUtil.format("{0}-{1}.{2}", artifactId, version, type); //$NON-NLS-1$
			Path localFile = localPath.resolve(fileName);
			
			if(!Files.isRegularFile(localFile)) {
				throw new RuntimeException("Unable to locate Maven artifact: " + localFile);
			}

			return localFile;
		}
	}

	public DominoContainer() {
		super(new DominoImage());

		withImagePullPolicy(imageName -> false);
		withExposedPorts(80);
		withStartupTimeout(Duration.ofMinutes(4));
		waitingFor(
			new WaitAllStrategy()
				.withStrategy(new LogMessageWaitStrategy()
					.withRegEx(".*Adding sign bit to.*") //$NON-NLS-1$
					.withTimes(300)
				)
				.withStrategy(new LogMessageWaitStrategy()
					.withRegEx(".*HTTP Server: Started.*") //$NON-NLS-1$
				)
				.withStrategy(new LogMessageWaitStrategy()
					.withRegEx(".*Done with postinstall.*") //$NON-NLS-1$
				)
			.withStartupTimeout(Duration.ofMinutes(3))
		);
	}
}
