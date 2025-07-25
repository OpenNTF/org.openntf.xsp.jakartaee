<%--

    Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>An MVC JSP File</title>
	</head>
	<body>
		<h1>I'm the MVC Guy!</h1>
		<p>From the URL, I got: ${incomingFoo}</p>
		<p>Application guy is ${applicationGuy.message}</p>
		<p>Request guy is ${requestGuy.message}</p>
		<p>DB is ${database}</p>
		<p>Session is ${dominoSession}</p>
		<p>Context from controller is ${contextFromController}</p>
		
		<t:example value="Value sent into the tag"/>
	</body>
</html>