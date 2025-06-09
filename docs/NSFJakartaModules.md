# NSF Jakarta Modules

NSF Jakarta Modules are a specialized type of NSF-based application that focuses on Jakarta technologies to the exclusion of legacy Domino web and XPages elements. They are developed the same way as normal NSFs, but have special traits when used in this way:

- They are mapped to a specific path unrelated to their NSF file path (e.g. "/myapp" references "/apps/myapp.nsf")
- The URL space within them is purely configured using Jakarta elements, similar to a normal WAR-based app
- These apps are initialized at HTTP start and their lifecycle continues until HTTP ends
- The workarounds used for Jakarta Faces in normal NSFs are not required
- File Resources, Images, Style Sheets, and WebContent files are accessible by URL the same way as in normal NSFs, but XPages and other design elements are not

### Configuring

Jakarta Modules are configured by creating an NSF using the included jakartaconfig.ntf. By default, this database should go on the server root as "jakartaconfig.nsf", but this can be configured using the `Jakarta_ConfigNSF` notes.ini property.

This database contains definitions for Jakarta NSF Modules, which consist of four settings:

- "Web Path", which is the base HTTP path the app will be accessible from
- "NSF Path", which is the path to the NSF containing the app
- "Servers", which is a multi-value list of servers that will run this app. This list can include groups and globs (e.g. "*/OU=AppServers/O=MyDomain")
- "Enabled", which can be set to "No" to disable an app without deleting it

### Developing

Jakarta Modules are developed in the same way as normal NSFs, using designer with the Jakarta libraries installed. NSFs of this type must have the `org.openntf.xsp.jakartaee.core` library enabled in Xsp Properties; the others are optional.

There are several techniques that are useful when developing apps of this type.

- `NotesContext` will not be available, but the JEE library provides `ComponentModuleLocator.getDefault()` to provide access to the contextual database, sessions, and ComponentModule in much the same way in both traditional and Jakarta Module apps
- Similarly, though `ExtLibUtil`, `FacesContext`, etc. won't function, CDI can (and should) be used to `@Inject` beans, Servlet, REST, and Domino objects in both types of apps
- `servletContext.getContextPath()` will return an appropriate base value for both types of apps (e.g. "/apps/foo.nsf" in a traditional context or "/foo" in a Jakarta module)
- When using Jakarta MVC, `${mvc.basePath}` can be used in Pages to retrieve the REST base path, avoiding the need to assume the "/xsp" prefix in view code
- Jakarta Modules don't use the "WEB-INF/jakarta" workarounds present for Faces in normal apps, since there is no XPages environment to conflict with