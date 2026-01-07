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
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Record Example</title>
	</head>
	<body>
		<span class="text-output"><c:out value="${RecordProducer.recordExample.name}"/></span>
		<span class="text-output2"><c:out value="${RecordProducer.optionalEmpty.name}"/></span>
		<span class="text-output3"><c:out value="${RecordProducer.optionalFull.name}"/></span>
		<span class="text-output4"><c:out value="${RecordProducer.recordExample.message}"/></span>
	</body>
</html>