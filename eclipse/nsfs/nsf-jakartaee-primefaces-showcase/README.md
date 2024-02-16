# PrimeFaces Showcase

This NSF is a port of the official PrimeFaces showcase app to NSF form. The contents are mostly unchanged, but there are a few distinctions:

- `FileContentMarkerUtil` is modified to add `if(is == null) { is = CDI.current().select(ServletContext.class).get().getResourceAsStream(path); }` after line 180, to account for Servlet implementation differences
- Removed the FacesServlet definition from WebContent/WEB-INF/web.xml, as this is provided by the framework