/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rest;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.openntf.xsp.jakarta.validation.XPagesValidationUtil;

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
	@Produces(MediaType.APPLICATION_JSON)
	public Object getRequestParam(
			@QueryParam("requiredField") @NotEmpty String requiredField
		) {
		return "Required field is: " + requiredField;
	}
	
	@GET
	@Path("requestValidation")
	@Produces({ MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getRequestParamXml(
			@QueryParam("requiredField") @NotEmpty String requiredFieldParam
		) {
		return "Required field is: " + requiredFieldParam;
	}
}