# Use in WebContainer Apps

This project provides a few hooks to ease the use of Jakarta EE technologies in PvC WebContainer webapps - those that use the `com.ibm.pvc.webcontainer.application` extension point and web.xml files. Some pieces - such as Validation - should be usable directly, but others take configuration.

## Servlets

The `org.openntf.xsp.jakarta.servlet` bundle contains a Servlet implementation class `org.openntf.xsp.jakarta.servlet.webapp.JakartaServletFacade`, which can be used to wrap a `jakarta.servlet.Servlet`/`HttpServlet` implementation class for use within Domino's Servlet 2.5 container. This can be done by specifying the `org.openntf.xsp.jakarta.servlet.class` init parameter:

```xml
<servlet>
	<servlet-name>ExampleServlet</servlet-name>
	<servlet-class>org.openntf.xsp.jakarta.servlet.webapp.JakartaServletFacade</servlet-class>
	
	<init-param>
		<param-name>org.openntf.xsp.jakarta.servlet.class</param-name>
		<param-value>org.openntf.xsp.jakarta.example.webapp.ExampleServlet</param-value>
	</init-param>
</servlet>
<servlet-mapping>
	<servlet-name>ExampleServlet</servlet-name>
	<url-pattern>/exampleServlet</url-pattern>
</servlet-mapping>
```

## JSP

The `org.openntf.xsp.jakarta.pages` bundle contains the Servlet class `org.openntf.xsp.jakarta.pages.webapp.WebappPagesServlet`, which can be used to provide JSP support by mapping it to `*.jsp`. For example, in web.xml:

```xml
<servlet>
	<servlet-name>JspServlet</servlet-name>
	<servlet-class>org.openntf.xsp.jakarta.pages.webapp.WebappPagesServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>JspServlet</servlet-name>
	<url-pattern>*.jsp</url-pattern>
</servlet-mapping>
```