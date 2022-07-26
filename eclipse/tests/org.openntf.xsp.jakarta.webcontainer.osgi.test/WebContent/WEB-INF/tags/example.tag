<%@tag description="I am an example tag" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@attribute name="value" required="true" type="java.lang.String" %>
<fieldset>
	<legend>I am example.tag</legend>
	
	<p>I was sent: ${pageScope.value}</p>
</fieldset>