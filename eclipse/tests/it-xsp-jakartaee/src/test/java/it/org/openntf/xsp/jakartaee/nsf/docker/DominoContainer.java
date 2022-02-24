/**
 * Copyright © 2018-2022 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.org.openntf.xsp.jakartaee.nsf.docker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;

public class DominoContainer extends GenericContainer<DominoContainer> {
	private static final String[] BUNDLE_DEPS = {
		"org.openntf.xsp.test.postinstall", //$NON-NLS-1$
		"org.openntf.xsp.test.beanbundle", //$NON-NLS-1$
	};
	
	public static final Set<Path> tempFiles = new HashSet<>();
	
	public DominoContainer() {
		super(DockerImageName.parse("hclcom/domino:latest")); //$NON-NLS-1$
		
		addEnv("LANG", "en_US.UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigure", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigureParams", "/local/runner/domino-config.json"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("DOMINO_DOCKER_STDOUT", "yes"); //$NON-NLS-1$ //$NON-NLS-2$

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
		
		// Build temp files to use as volume binds
		try {
			String version = getMavenVersion();

			// Compose an Eclipse directory for the data-dir installation path in the container
			Path eclipse = Files.createTempDirectory(getClass().getName());
			tempFiles.add(eclipse);
			String pathSep = eclipse.getFileSystem().getSeparator();
			
			Path updateSite = findLocalMavenArtifact("org.openntf.xsp", "org.openntf.xsp.jakartaee.updatesite", version, "zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// Copy in the project update site
			try(
				InputStream is = Files.newInputStream(updateSite);
				ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)
			) {
				for(ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
					if(!entry.isDirectory()) {
						Path targetPath = eclipse.resolve(entry.getName().replace("/", pathSep)); //$NON-NLS-1$
						Files.createDirectories(targetPath.getParent());
						Files.copy(zis, targetPath);
					}
				}
			}
			
			// Copy in the test-support bundles
			for(String bundleName : BUNDLE_DEPS) {
				Path bundle = findLocalMavenArtifact("org.openntf.xsp", bundleName, version, "jar"); //$NON-NLS-1$ //$NON-NLS-2$
				
				Path targetPath = eclipse.resolve("plugins").resolve(bundleName + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
				Files.createDirectories(targetPath.getParent());
				Files.copy(bundle, targetPath);
			}
			
			addFileSystemBind(eclipse.toString(), "/local/eclipse/eclipse", BindMode.READ_WRITE); //$NON-NLS-1$

			
			// Create an Equinox link to the above
			Path links = Files.createTempDirectory(DominoContainer.class.getName());
			tempFiles.add(links);
			try(InputStream is = getClass().getResourceAsStream("/docker/container.link")) { //$NON-NLS-1$
				Files.copy(is, links.resolve("container.link")); //$NON-NLS-1$
			}
			addFileSystemBind(links.toString(), "/opt/hcl/domino/notes/latest/linux/osgi/rcp/eclipse/links", BindMode.READ_ONLY); //$NON-NLS-1$
			
			
			// Next up, copy our Java policy to be the Notes home dir in the container
			Path notesHome = Files.createTempDirectory(DominoContainer.class.getName());
			tempFiles.add(notesHome);
			try(InputStream is = getClass().getResourceAsStream("/docker/java.policy")) { //$NON-NLS-1$
				Files.copy(is, notesHome.resolve(".java.policy")); //$NON-NLS-1$
			}
			addFileSystemBind(notesHome.toString(), "/home/notes", BindMode.READ_WRITE); //$NON-NLS-1$
			
			// Finally, add our NTFs and Domino config to /local/runner
			Path runner = Files.createTempDirectory(DominoContainer.class.getName());
			tempFiles.add(runner);
			{
				Path exampleNtf = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaee-example", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Path exampleNtfDest = runner.resolve("jakartaee.ntf"); //$NON-NLS-1$
				Files.copy(exampleNtf, exampleNtfDest);
			}
			{
				Path bundleExampleNtf = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaee-bundle-example", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Path bundleExampleNtfDest = runner.resolve("jeebundle.ntf"); //$NON-NLS-1$
				Files.copy(bundleExampleNtf, bundleExampleNtfDest);
			}
			{
				Path baseBundleExampleNtf = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaee-bundlebase-example", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Path baseBundleExampleNtfDest = runner.resolve("jeebasebundle.ntf"); //$NON-NLS-1$
				Files.copy(baseBundleExampleNtf, baseBundleExampleNtfDest);
			}
			{
				Path configJson = runner.resolve("domino-config.json"); //$NON-NLS-1$
				try(InputStream is = getClass().getResourceAsStream("/docker/domino-config.json")) { //$NON-NLS-1$
					Files.copy(is, configJson);
				}
			}
			addFileSystemBind(runner.toString(), "/local/runner", BindMode.READ_WRITE); //$NON-NLS-1$
			
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static Path findLocalMavenArtifact(String groupId, String artifactId, String version, String type) {
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
	
	private static String getMavenVersion() {
		// Find the current build version
		Properties props = new Properties();
		try (InputStream is = DominoContainer.class.getResourceAsStream("/scm.properties")) { //$NON-NLS-1$
			props.load(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		String version = props.getProperty("git.build.version", null); //$NON-NLS-1$
		if(StringUtil.isEmpty(version)) {
			throw new RuntimeException("Unable to determine artifact version from scm.properties");
		}
		return version;
	}
}
