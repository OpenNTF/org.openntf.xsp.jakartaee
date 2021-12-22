package test;

import java.io.IOException;
import java.io.StringWriter;

import org.openntf.xsp.jakartaee.xml.JaxbUtil;

import bean.ApplicationGuy;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;

public class XmlGuy {
	public String getBeanXml() throws JAXBException, IOException {
		ApplicationGuy bean = CDI.current().select(ApplicationGuy.class).get();
		JAXBContext context = JaxbUtil.newInstance(ApplicationGuy.class);
	    Marshaller mar= context.createMarshaller();
	    mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		try(StringWriter w = new StringWriter()) {
			mar.marshal(bean, w);
			w.flush();
			return w.toString();
		}
	}
}
