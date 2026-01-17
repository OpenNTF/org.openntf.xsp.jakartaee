<%--

    Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project

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
<!DOCTYPE html>
<html>
	<head>
		<title>CDI HTTP Context Tester - Issue #455</title>
	</head>
	<body>
    	
    	<dl>
    		<dt>httpContextGuy.request.contextPath</dt>
    		<dd>${httpContextGuy.request.contextPath}</dd>
    		
    		<dt>httpContextGuy.response.status</dt>
    		<dd>${httpContextGuy.response.status}</dd>
    		
    		<dt>httpContextGuy.context.serverInfo</dt>
    		<dd>${httpContextGuy.context.serverInfo}</dd>
    	</dl>
	</body>
</html>