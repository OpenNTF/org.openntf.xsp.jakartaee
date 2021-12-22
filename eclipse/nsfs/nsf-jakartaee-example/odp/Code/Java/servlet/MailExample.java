package servlet;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/mail")
public class MailExample {
	@GET
	public Object getMultipart() throws MessagingException {
		MimeMultipart result = new MimeMultipart();
		MimeBodyPart part = new MimeBodyPart();
		part.setContent("i am content", "text/plain");
		result.addBodyPart(part);
		result.setPreamble("I am preamble");
		return result;
	}
}
