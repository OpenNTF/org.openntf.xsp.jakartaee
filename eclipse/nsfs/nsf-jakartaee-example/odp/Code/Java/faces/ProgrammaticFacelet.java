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
package faces;

import java.io.IOException;
import java.util.List;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.annotation.View;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.component.html.HtmlOutputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

@View("/programmaticFacelet.xhtml")
@ApplicationScoped
public class ProgrammaticFacelet extends Facelet {

	@Override
	public void apply(FacesContext facesContext, UIComponent parent) throws IOException {
		List<UIComponent> rootChildren = parent.getChildren();
		if(!rootChildren.isEmpty()) {
			return;
		}
		
		UIOutput front = new UIOutput();
		front.setValue("<!DOCTYPE html>\n<html>");
		rootChildren.add(front);
		
		HtmlBody body = create(facesContext, HtmlBody.COMPONENT_TYPE);
		rootChildren.add(body);
		
		UIOutput title = new UIOutput();
		title.setValue("<h1>I am a programmatic Facelet</h1>");
		body.getChildren().add(title);
		
		HtmlOutputText text = create(facesContext, HtmlOutputText.COMPONENT_TYPE);
		text.setId("paramOutput");
		text.setStyleClass("param-output");
		ELContext elContext = facesContext.getELContext();
		ValueExpression exp = facesContext.getApplication().getExpressionFactory().createValueExpression(elContext, "#{param.foo}", String.class);
		text.setValueExpression("value", exp);
		body.getChildren().add(text);
		
		UIOutput back = new UIOutput();
		back.setValue("</html>");
		rootChildren.add(back);
	}

	@SuppressWarnings("unchecked")
	private <T> T create(FacesContext facesContext, String type) {
		return (T) facesContext.getApplication().createComponent(facesContext, type, null);
	}

}
