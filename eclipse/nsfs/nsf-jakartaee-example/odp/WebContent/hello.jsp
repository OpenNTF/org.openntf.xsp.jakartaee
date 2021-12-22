<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>JSP Inside An NSF</title>
	</head>
	<body>
		<p>My CDI Bean is: ${applicationGuy}</p>
		<p>My requestScope is: ${requestScope}</p>
		<p>JSTL XML-escaped content is: ${fn:escapeXml('<hello>')}</p>
		
		<t:example value="Value sent into the tag"/>
	</body>
</html>