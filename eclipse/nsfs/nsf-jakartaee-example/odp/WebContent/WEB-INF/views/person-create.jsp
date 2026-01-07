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
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Create New Person</title>
	</head>
	<body>
		<form method="POST" enctype="multipart/form-data" action="create">
			<dl>
				<dt>First Name</dt>
				<dd><input type="text" required name="firstName" /></dd>
				
				<dt>Last Name</dt>
				<dd><input type="text" required name="lastName" /></dd>
				
				<dt>Birthday</dt>
				<dd><input type="date" name="birthday" /></dd>
				
				<dt>Favorite Time</dt>
				<dd><input type="time" name="favoriteTime" value="${person.favoriteTime}" /></dd>
				
				<dt>Added</dt>
				<dd><input type="datetime-local" name="added" value="${personadded}" /></dd>
				
				<dt>Custom Property</dt>
				<dd><input type="text" name="customProperty" /></dd>
				
				<dt>Intentionally Fail Commit</dt>
				<dd><input type="checkbox" name="intentionallyRollBack" value="true"/></dd>
				
				<dt>Attachment</dt>
				<dd><input type="file" name="attachment" /></dd>
			</dl>
			<input type="submit" value="Create Person" />
		</form>
	</body>
</html>