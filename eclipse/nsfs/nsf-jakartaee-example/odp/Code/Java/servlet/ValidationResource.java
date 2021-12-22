package servlet;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.openntf.xsp.beanvalidation.XPagesValidationUtil;

import bean.ValidationBean;

@Path("/validation")
public class ValidationResource {
	@GET
	@Path("valid")
	@Produces(MediaType.TEXT_PLAIN)
	public Object getValid() {
		ValidationBean bean = new ValidationBean();
		bean.setFoo("hey");
		bean.setBar("there");
		
		Validator validator = XPagesValidationUtil.constructGenericValidator();
		return XPagesValidationUtil.validateBean(bean, validator);
	}
	
	@GET
	@Path("invalid")
	@Produces(MediaType.TEXT_PLAIN)
	public Object getInvalid() {
		ValidationBean bean = new ValidationBean();
		bean.setFoo(null);
		bean.setBar("there!!");
		
		Validator validator = XPagesValidationUtil.constructGenericValidator();
		return XPagesValidationUtil.validateBean(bean, validator);
	}
	
	@GET
	@Path("requestValidation")
	@Produces(MediaType.TEXT_PLAIN)
	public Object getRequestParam(
			@QueryParam("requiredField") @NotEmpty String requiredField
		) {
		return "Required field is: " + requiredField;
	}
}
