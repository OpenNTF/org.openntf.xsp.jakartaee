# Use in WebContainer Apps

This project provides a few hooks to ease the use of Jakarta EE technologies in WebContainer webapps - those that use the `com.ibm.pvc.webcontainer.application` extension point and web.xml files. Some pieces - such as Bean Validation - should be usable directly, but others take configuration.

## JSP

The `org.openntf.xsp.jsp` bundle contains the Servlet class `org.openntf.xsp.jsp.webapp.WebappJspServlet`, which can be used to provide JSP support by mapping it to `*.jsp`. For example, in web.xml:

```xml
<servlet>
	<servlet-name>JspServlet</servlet-name>
	<servlet-class>org.openntf.xsp.jsp.webapp.WebappJspServlet</servlet-class>
</servlet>
<servlet-mapping>
	<servlet-name>JspServlet</servlet-name>
	<url-pattern>*.jsp</url-pattern>
</servlet-mapping>
```