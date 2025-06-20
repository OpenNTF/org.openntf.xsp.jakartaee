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
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>Person List</title>
	</head>
	<body>
		<table>
			<thead>
				<tr>
					<c:if test="${param.sortCol == 'lastName'}">
						<th>Last Name &#x25b2;</th>
					</c:if>
					<c:if test="${param.sortCol != 'lastName'}">
						<th><a href="list?sortCol=lastName">Last Name</a></th>
					</c:if>
					<c:if test="${param.sortCol == 'firstName'}">
						<th>First Name &#x25b2;</th>
					</c:if>
					<c:if test="${param.sortCol != 'firstName'}">
						<th><a href="list?sortCol=firstName">First Name</a></th>
					</c:if>
					<th>Birthday</th>
					<th>Favorite Time</th>
					<th>Added</th>
					<th>Custom Property</th>
					<th>Attachments</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${persons}" var="person">
					<tr>
						<td><a href="${person.unid}">${fn:escapeXml(person.lastName)}</a></td>
						<td>${fn:escapeXml(person.firstName)}</td>
						<td>${fn:escapeXml(person.birthday)}</td>
						<td>${fn:escapeXml(person.favoriteTime)}</td>
						<td>${fn:escapeXml(person.added)}</td>
						<td>${fn:escapeXml(person.customProperty)}</td>
						<td>
							<c:forEach items="${person.attachments}" var="att">
								<a href="${person.unid}/attachment/${encoderBean.urlEncode(att.name)}">${fn:escapeXml(att.name)}</a>
							</c:forEach>
						</td>
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