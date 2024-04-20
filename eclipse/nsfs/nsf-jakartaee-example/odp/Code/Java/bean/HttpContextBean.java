package bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Used to test Issue #455
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/455">Issue #455</a>
 */
@RequestScoped
@Named("httpContextGuy")
public class HttpContextBean {
	@Inject
	private HttpServletRequest request;
	
	@Inject
	private HttpServletResponse response;
	
	@Inject
	private ServletContext context;
	
	public HttpServletRequest getRequest() {
		return request;
	}
	public HttpServletResponse getResponse() {
		return response;
	}
	public ServletContext getContext() {
		return context;
	}
}
