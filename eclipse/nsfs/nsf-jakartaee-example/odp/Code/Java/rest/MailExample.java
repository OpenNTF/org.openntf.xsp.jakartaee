/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import com.ibm.commons.util.io.StreamUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/mail")
public class MailExample {
	
	@Context
	HttpServletRequest request;
	
	@Path("multipart")
	@GET
	public Object getMultipart() throws MessagingException {
		MimeMultipart result = new MimeMultipart();
		MimeBodyPart part = new MimeBodyPart();
		part.setContent("i am content", "text/plain");
		result.addBodyPart(part);
		result.setPreamble("I am preamble");
		return result;
	}
	
	// Call back in this service so that the above triggers the multipart writer,
	//  but this writes the content in a browser-friendly way 
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Object get() throws MalformedURLException, IOException {
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("mail/multipart");
		try(InputStream is = serviceUri.toURL().openStream()) {
			return StreamUtil.readString(is);
		}
	}
}
