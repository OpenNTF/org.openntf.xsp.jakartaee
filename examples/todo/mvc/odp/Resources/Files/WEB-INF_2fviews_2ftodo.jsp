<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<t:layout>
	<form action="${mvc.basePath}/todos/${todo.documentId}" method="POST" enctype="application/x-www-form-urlencoded">
		<dl>
			<dt>Created</dt>
			<dd><c:out value="${todo.created}"/></dd>
			
			<dt>Title</dt>
			<dd><input name="title" type="text" value="${fn:escapeXml(todo.title)}"/></dd>
			
			<dt>Status</dt>
			<dd>
				<input name="status" type="radio" value="Incomplete" ${todo.status == 'Incomplete' ? 'checked' : ''}> Incomplete
				<input name="status" type="radio" value="Complete" ${todo.status == 'Complete' ? 'checked' : ''}> Complete
			</dd>
		</dl>
		
		<input type="submit" value="Save"/>
	</form>
</t:layout>