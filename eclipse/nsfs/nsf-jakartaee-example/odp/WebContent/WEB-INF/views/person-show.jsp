<%--

    Copyright © 2018-2022 Jesse Gallagher

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
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Person</title>
	</head>
	<body>
		<form method="POST" enctype="application/x-www-form-urlencoded" action="${person.unid}/update">
			<dl>
				<dt>First Name</dt>
				<dd><input type="text" required name="firstName" value="${person.firstName}" /></dd>
				
				<dt>Last Name</dt>
				<dd><input type="text" required name="lastName" value="${person.lastName}" /></dd>
			</dl>
			<input type="submit" value="Save" />
		</form>
	</body>
</html>