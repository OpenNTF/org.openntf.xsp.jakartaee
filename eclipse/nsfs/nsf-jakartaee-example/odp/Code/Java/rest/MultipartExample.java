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
package rest;

import java.io.IOException;
import java.text.MessageFormat;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;

@Path("/restMultipart")
public class MultipartExample {
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public String post(@FormParam("name") String name, @FormParam("part") EntityPart part) throws IOException {
		byte[] data = part.getContent().readAllBytes();
		
		return MessageFormat.format("You sent me name={0} and a part of {1} bytes", name, data.length);
	}
}
