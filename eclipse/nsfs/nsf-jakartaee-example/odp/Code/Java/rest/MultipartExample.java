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
