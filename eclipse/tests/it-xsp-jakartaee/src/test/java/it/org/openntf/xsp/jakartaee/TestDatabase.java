package it.org.openntf.xsp.jakartaee;

/**
 * Represents the test NSFs deployed to the container
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
@SuppressWarnings("nls")
public enum TestDatabase {
	MAIN("nsf-jakartaee-example", "jakartaee", "XPages JEE Example"),
	BUNDLE("nsf-jakartaee-bundle-example", "jeebundle", "XPages JEE Bundle Example"),
	BUNDLEBASE("nsf-jakartaee-bundlebase-example", "jeebasebundle", "XPages JEE Bundle Base Example"),
	JSONB_CONFIG("nsf-jakartaee-jsonbconfig-example", "jsonbconfig", "XPages JEE JsonbConfig Example"),
	JPA("nsf-jakartaee-jpa-example", "jpa", "XPages JEE JPA Example"),
	PRIMEFACES("nsf-jakartaee-primefaces-example", "primefaces", "XPages JEE PrimeFaces Example")
	;
	
	private final String artifactId;
	private final String fileName;
	private final String title;
	
	private TestDatabase(String artifactId, String fileName, String title) {
		this.artifactId = artifactId;
		this.fileName = fileName;
		this.title = title;
	}
	
	public String getArtifactId() {
		return artifactId;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getContextPath() {
		return "/dev/" + fileName + ".nsf";
	}
}
