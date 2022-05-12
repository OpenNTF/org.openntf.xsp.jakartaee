/*
 * � Copyright IBM Corp. 2011, 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.openntf.xsp.jakartaee.designer.props.pages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.openntf.xsp.jakartaee.designer.props.JakartaAllProperties;
import org.openntf.xsp.jakartaee.designer.props.editor.JakartaParentEditor;

import com.ibm.commons.iloader.node.validators.IntegerValidator;
import com.ibm.commons.swt.controls.custom.CustomCheckBox;
import com.ibm.commons.swt.data.controls.DCComboBox;
import com.ibm.commons.swt.data.controls.DCPanel;
import com.ibm.commons.swt.data.controls.DCText;
import com.ibm.xsp.extlib.designer.xspprops.XSPEditorUtil;

// NB: WidgetProperties must be used because the replacement doesn't exist in Designer's platform
@SuppressWarnings("deprecation")
public class MainPage extends DCPanel {
	private FormToolkit toolkit;
	private JakartaAllProperties props;

	private DCPanel leftComposite = null;
	@SuppressWarnings("unused")
	private DCPanel rightComposite = null;

	private CustomCheckBox corsEnable;
	private List<Control> corsControls = new ArrayList<>();
	
	private DataBindingContext binding;

	public MainPage(Composite parent, FormToolkit ourToolkit, JakartaParentEditor editor) {
		super(parent, SWT.NONE);
		this.toolkit = ourToolkit;
//		this.props = editor.getDBPropObject().getXspProperties();
//		this.binding = new DataBindingContext();

		initialize();
	}
	
	@Override
	public void dispose() {
		binding.dispose();
		super.dispose();
	}

	private ScrolledForm initialize() {
		setParentPropertyName("xspProperties"); //$NON-NLS-1$
		GridLayout ourLayout = new GridLayout(1, false);
		ourLayout.marginHeight = 0;
		ourLayout.marginWidth = 0;
		setLayout(ourLayout);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

		ScrolledForm scrolledForm = toolkit.createScrolledForm(this);
		scrolledForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		Composite formatComposite = XSPEditorUtil.createFormComposite(scrolledForm);

		XSPEditorUtil.createCLabel(formatComposite, "Jakarta EE Properties", 2);

		createLeftSide(formatComposite);
		createRightSide(formatComposite);
		return scrolledForm;
	}

	private void createLeftSide(Composite formatComposite) {
		leftComposite = XSPEditorUtil.createDCPanel(formatComposite, 1, "xspProperties", "leftComp"); //$NON-NLS-1$ //$NON-NLS-2$
		GridData gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 4);
		// TODO figure out how best to a min width
		gridData.minimumWidth = 300;
		leftComposite.setLayoutData(gridData);

		createRestArea(leftComposite);
	}

	private void createRightSide(Composite formatComposite) {
		DCPanel rightComposite = XSPEditorUtil.createDCPanel(formatComposite, 1, "xspProperties", "rightComp"); //$NON-NLS-1$ //$NON-NLS-2$
		GridData gridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 4);
		// TODO figure out how best to a min width
		gridData.minimumWidth = 300;
		rightComposite.setLayoutData(gridData);

		createElArea(rightComposite);
		createJsfArea(rightComposite);
	}

	private void createElArea(Composite parent) {
		Section general = XSPEditorUtil.createSection(toolkit, parent, "Expression Language", 1, 1);
		Composite generalContainer = XSPEditorUtil.createSectionChild(general, 2);
		generalContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));

		// EL Prefix
		{
			Label label = XSPEditorUtil.createLabel(generalContainer, "XPages EL Prefix:", 1);
			label.setToolTipText("Specifies an override prefix to use the Jakarta EL interpreter");
			// TODO finish work of switching the binding type
			// https://www.vogella.com/tutorials/EclipseDataBinding/article.html
			XSPEditorUtil.createText(generalContainer, "elPrefix", 1, 0, 0); //$NON-NLS-1$
//			Text text = new Text(generalContainer, SWT.BORDER);
//			text.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 1, 1));
//			bindText(this.binding, text, props, "elPrefix"); //$NON-NLS-1$
		}

		general.setClient(generalContainer);
	}

	private void createJsfArea(Composite parent) {
		Section general = XSPEditorUtil.createSection(toolkit, parent, "Jakarta Faces", 1, 1);
		Composite generalContainer = XSPEditorUtil.createSectionChild(general, 2);
		generalContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));

		// Project Stage
		{
			XSPEditorUtil.createLabel(generalContainer, "Project Stage:", 1);
			DCComboBox projectStage = XSPEditorUtil.createDCCombo(generalContainer, "facesProjectStage", 1, false, //$NON-NLS-1$
					false);
			projectStage.setEditableLabels(true);
			projectStage.setItems("Development", "UnitTest", "SystemTest", "Production"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		general.setClient(generalContainer);
	}

	private void createRestArea(Composite parent) {
		Section general = XSPEditorUtil.createSection(toolkit, parent, "RESTful Web Services", 1, 1);
		Composite generalContainer = XSPEditorUtil.createSectionChild(general, 2);
		generalContainer.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));

		// Base Path
		{
			Label label = XSPEditorUtil.createLabel(generalContainer, "Base Path:", 1);
			label.setToolTipText("Specifies the base path for services beneath \"/xsp\" within the NSF");
			XSPEditorUtil.createText(generalContainer, "restBasePath", 1, 0, 0); //$NON-NLS-1$
		}

		// CORS
		{
			corsEnable = XSPEditorUtil.createIndentedCheck(generalContainer, "Enable CORS", "corsEnable", 0); //$NON-NLS-2$
			corsEnable.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					enableOptions();
				}
			});
			corsEnable.setLayoutData(new GridData(1, 1, false, false, 2, 1));

			CustomCheckBox cred = XSPEditorUtil.createIndentedCheck(generalContainer, "Allow Credentials",
					"corsAllowCredentials", 18); //$NON-NLS-1$
			GridData credGridData = new GridData(1, 1, false, false, 2, 1);
			credGridData.horizontalIndent = 18;
			cred.setLayoutData(credGridData);
			corsControls.add(cred);

			corsControls.add(XSPEditorUtil.createIndentedLabel(generalContainer, "Allowed Methods:", 1, 18));
			DCText methods = XSPEditorUtil.createText(generalContainer, "corsAllowedMethods", 1, 0, 0); //$NON-NLS-1$
			corsControls.add(methods);

			corsControls.add(XSPEditorUtil.createIndentedLabel(generalContainer, "Allowed Headers:", 1, 18));
			DCText headers = XSPEditorUtil.createText(generalContainer, "corsAllowedHeaders", 1, 0, 0); //$NON-NLS-1$
			corsControls.add(headers);

			corsControls.add(XSPEditorUtil.createIndentedLabel(generalContainer, "Exposed Headers:", 1, 18));
			DCText exposed = XSPEditorUtil.createText(generalContainer, "corsExposedHeaders", 1, 0, 0); //$NON-NLS-1$
			corsControls.add(exposed);

			corsControls.add(XSPEditorUtil.createIndentedLabel(generalContainer, "Max Age:", 1, 18));
			DCText maxAge = XSPEditorUtil.createText(generalContainer, "corsMaxAge", 1, 0, 0); //$NON-NLS-1$
			maxAge.setValidator(IntegerValidator.positiveInstance);
			corsControls.add(maxAge);

			corsControls.add(XSPEditorUtil.createIndentedLabel(generalContainer, "Allowed Origins:", 1, 18));
			DCText origins = XSPEditorUtil.createText(generalContainer, "corsAllowedOrigins", 1, 0, 0); //$NON-NLS-1$
			corsControls.add(origins);
		}

		general.setClient(generalContainer);
	}

	public void enableOptions() {
		boolean corsEnabled = corsEnable.getSelection();
		corsControls.forEach(c -> c.setEnabled(corsEnabled));
	}

	public void initProject() {
		getDataNode().notifyInvalidate(null);
		enableOptions();
	}
	
	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************
	
	@SuppressWarnings({ "unchecked" })
	private static void bindText(DataBindingContext ctx, Text text, JakartaAllProperties props, String property) {
		IObservableValue<?> target = WidgetProperties.text(SWT.Modify).observe(text);
		IObservableValue<?> model = BeanProperties.value(property).observe(props);
		
		ctx.bindValue(target, model);
	}
}