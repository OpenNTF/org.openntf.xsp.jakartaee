/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
	PRIMEFACES("nsf-jakartaee-primefaces-example", "primefaces", "XPages JEE PrimeFaces Example"),
	OSGI_WEBAPP(null, null, "Example OSGi Webapp") {
		@Override
		public String getContextPath() {
			return "/jeeExample";
		}
	},
	PRIMEFACES_SHOWCASE("nsf-jakartaee-primefaces-showcase", "primefaces-showcase", "XPages JEE PrimeFaces Showcase"),
	MAIN_MODULE(null, null, "Jakarta NSF Module") {
		@Override
		public String getContextPath() {
			return "/moduleapp";
		}
		
		@Override
		public String getXspPrefix() {
			return "";
		}
	},
	DISABLED_MODULE(null, null, "Disabled Jakarta NSF Module") {
		@Override
		public String getContextPath() {
			return "/moduleappdisabled";
		}
	}
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
	
	public boolean isNsf() {
		return artifactId != null;
	}
	
	public String getContextPath() {
		return "/dev/" + fileName + ".nsf";
	}
	
	public String getXspPrefix() {
		return "/xsp";
	}
}
