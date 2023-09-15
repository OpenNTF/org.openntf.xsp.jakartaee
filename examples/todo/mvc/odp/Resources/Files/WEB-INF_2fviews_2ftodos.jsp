<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<t:layout>
	<div id="todos">
		<table>
			<thead>
				<tr>
					<th>Created</th>
					<th>Title</th>
					<th>Status</th>
					<th></th>
				</tr>
			</thead>
			<tbody>
			<c:forEach items="${todos}" var="todo">
				<tr>
					<td><c:out value="${todo.created}"/></td>
					<td><a href="${mvc.basePath}/todos/${todo.documentId}"><c:out value="${todo.title}"/></a></td>
					<td><c:out value="${todo.status}"/></td>
					<td>
						<form action="${mvc.basePath}/todos/${todo.documentId}/delete" method="POST" enctype="application/x-www-form-urlencoded">
							<input type="submit" value="Delete" onclick="return confirm('Delete this To-Do?')"/>
						</form>
						
					</td>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</div>
	
	<c:if test="${param.status ne 'Completed'}">
	<fieldset>
		<legend>New To-Do</legend>
		
		<form action="${mvc.basePath}/todos" method="POST" enctype="application/x-www-form-urlencoded">
			<dl>
				<dt>Title</dt>
				<dd><input name="title" type="text"></dd>
			</dl>
			
			<input type="submit" value="Save"/>
		</form>
	</fieldset>
	</c:if>
</t:layout>