package it.org.openntf.xsp.jakartaee;

/**
 * Represents the test NSFs deployed to the container
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
@SuppressWarnings("nls")
public enum TestDatabase {
	MAIN("nsf-jakartaee-example", "jakartaee"),
	BUNDLE("nsf-jakartaee-bundle-example", "jeebundle"),
	BUNDLEBASE("nsf-jakartaee-bundlebase-example", "jeebasebundle"),
	JSONB_CONFIG("nsf-jakartaee-jsonbconfig-example", "jsonbconfig"),
	JPA("nsf-jakartaee-jpa-example", "jpa"),
	;
	
	private final String artifactId;
	private final String fileName;
	
	private TestDatabase(String artifactId, String fileName) {
		this.artifactId = artifactId;
		this.fileName = fileName;
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public String getFileName() {
		return fileName;
	}
}
