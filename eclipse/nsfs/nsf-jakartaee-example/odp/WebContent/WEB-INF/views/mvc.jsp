<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>An MVC JSP File</title>
	</head>
	<body>
		<h1>I'm the MVC Guy!</h1>
		<p>From the URL, I got: ${incomingFoo}</p>
		<p>Application guy is ${applicationGuy.message}</p>
		<p>DB is ${database}</p>
		<p>Session is ${session}</p>
		<p>Context from controller is ${contextFromController}</p>
		
		<t:example value="Value sent into the tag"/>
	</body>
</html>