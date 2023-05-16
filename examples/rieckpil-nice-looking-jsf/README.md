# Rieckpil.de PrimeFaces Example

This directory contains a near-exact poet of the ["Create nice-looking JSF 2.3 applications with PrimeFaces"](*https://rieckpil.de/howto-create-nice-looking-jsf-2-3-applications-with-primefaces-7-0*) example app from rieckpil.de, which is hosted on GitHub at [*https://github.com/rieckpil/blog-tutorials/tree/master/nice-looking-jsf-apps-with-prime-faces-7*](*https://github.com/rieckpil/blog-tutorials/tree/master/nice-looking-jsf-apps-with-prime-faces-7*).

There are a handful of changes, but the app is largely kept as-is:

- Switch all JEE imports from `javax.*` to `jakarta.*`
- Instead of a Maven layout, components are placed in the NSF, with code in `Code/Java`, Faces elements within `WebContent`, and the PrimeFaces JAR in `WebContent/WEB-INF/jakarta/lib`
- Due to the lack of EJB in the XPages JEE project, the timed auto-reset of in-memory data is removed
- To avoid ClassLoader trouble, the code in `IndexBean` that references PrimeFaces classes uses reflection. Due to this, running this likely requires relaxing your java.policy settings on Domino
- The random name generation is less realistic, to avoid including another dependency
- The use of `url="index.html"` et al for internal pages in header.xhtml was changed to `outcome="/index"` to work within the NSF URL path
- This adds a `AppFacesConfig` class annotated with `@FacesConfig`, which acts as a hint to the JSF runtime to resolve CDI beans