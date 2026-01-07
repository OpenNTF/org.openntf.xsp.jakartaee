/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
