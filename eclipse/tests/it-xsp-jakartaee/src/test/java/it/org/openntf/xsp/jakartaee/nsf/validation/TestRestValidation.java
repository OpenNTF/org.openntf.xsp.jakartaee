/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;

@SuppressWarnings("nls")
public class TestRestValidation extends AbstractWebClientTest {
	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testValid(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/validation/requestValidation?requiredField=hello");
		Response response = target.request().get();
		
		String body = response.readEntity(String.class);
		assertEquals("Required field is: hello", body);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testInvalid(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/validation/requestValidation");
		Response response = target.request("text/html","application/xhtml+xml","application/xml","*/*").get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.contains("<violationReport>"
				+ "<parameterViolations>"
				+ "<constraintType>PARAMETER</constraintType>"
				+ "<path>getRequestParamXml.requiredFieldParam</path>"
				+ "<message>must not be empty</message>"
				+ "<value></value>"
				+ "</parameterViolations>"
				+ "</violationReport>"), () -> "Unexpected result: " + body);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testInvalidXml(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/validation/requestValidation");
		Response response = target.request("application/xml").get();
		
		String body = response.readEntity(String.class);
		assertTrue(body.contains("<violationReport>"
				+ "<parameterViolations>"
				+ "<constraintType>PARAMETER</constraintType>"
				+ "<path>getRequestParamXml.requiredFieldParam</path>"
				+ "<message>must not be empty</message>"
				+ "<value></value>"
				+ "</parameterViolations>"
				+ "</violationReport>"), () -> "Unexpected result: " + body);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumOnly.class)
	public void testInvalidJson(TestDatabase db) {
		Client client = getAnonymousClient();
		WebTarget target = client.target(getRestUrl(null, db) + "/validation/requestValidation");
		Response response = target.request("application/json").get();
		
		String body = response.readEntity(String.class);
		assertEquals("{\"classViolations\":[],\"parameterViolations\":[{\"constraintType\":\"PARAMETER\",\"message\":\"must not be empty\",\"path\":\"getRequestParam.requiredField\",\"value\":\"\"}],\"propertyViolations\":[],\"returnValueViolations\":[]}", body);
	}
}
