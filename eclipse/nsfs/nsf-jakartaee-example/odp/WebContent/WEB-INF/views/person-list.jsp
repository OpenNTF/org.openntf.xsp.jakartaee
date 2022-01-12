<%--

    Copyright � 2018-2022 Jesse Gallagher

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
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Person List</title>
	</head>
	<body>
		<table>
			<thead>
				<tr>
					<th>Last Name</th>
					<th>First Name</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${persons}" var="person">
					<tr>
						<td><a href="${person.unid}">${fn:escapeXml(person.lastName)}</a></td>
						<td>${fn:escapeXml(person.firstName)}</td>
						<td>
							<form method="POST" action="${person.unid}/delete">
								<input type="submit" value="Delete"/>
							</form>
						</td>
					</tr>
					
				</c:forEach>
			</tbody>
		</table>
				
		<hr />
		
		<a href="create">Create New</a>
	</body>
</html>