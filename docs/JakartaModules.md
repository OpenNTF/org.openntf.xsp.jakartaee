# Jakarta Modules

Jakarta Modules are a specialized type of NSF-based application that focuses on Jakarta technologies to the exclusion of legacy Domino web and XPages elements. They are developed the same way as normal NSFs, but have special traits when used in this way:

- They are mapped to a specific path unrelated to their NSF file path (e.g. "/myapp" references "/apps/myapp.nsf")
- The URL space within them is purely configured using Jakarta elements, similar to a normal WAR-based app
- These apps are initialized at HTTP start and their lifecycle continues until HTTP ends
- The workarounds used for Jakarta Faces in normal NSFs are not required
- File Resources, Images, Style Sheets, and WebContent files are accessible by URL the same way as in normal NSFs, but XPages and other design elements are not

### Configuring

Jakarta Modules are configured by creating an NSF using the included jakartaconfig.ntf. By default, this database should go on the server root as "jakartaconfig.nsf", but this can be configured using the `Jakarta_ConfigNSF` notes.ini property.

This database contains definitions for Jakarta NSF Modules, which consist of three settings:

- "Web Path", which is the base HTTP path the app will be accessible from
- "NSF Path", which is the path to the NSF containing the app
- "Servers", which is a multi-value list of servers that will run this app. This list can include groups and globs (e.g. "*/OU=AppServers/O=MyDomain")