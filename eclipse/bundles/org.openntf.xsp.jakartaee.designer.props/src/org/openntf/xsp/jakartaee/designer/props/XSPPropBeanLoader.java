/*
 * � Copyright IBM Corp. 2011
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
package org.openntf.xsp.jakartaee.designer.props;

import java.util.HashMap;
import java.util.Properties;

import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.part.FileEditorInput;
import org.openntf.xsp.jakartaee.designer.props.editor.JakartaParentEditor;

import com.ibm.commons.iloader.node.DataChangeNotifier;
import com.ibm.commons.iloader.node.IAttribute;
import com.ibm.commons.iloader.node.NodeException;
import com.ibm.commons.iloader.node.loaders.JavaBeanLoader;
import com.ibm.xsp.extlib.designer.common.properties.XSPPropertiesUtils;

@SuppressWarnings("restriction")
public class XSPPropBeanLoader extends JavaBeanLoader {
    private HashMap<String, String> modList;
    private FileEditorInput fei = null;
    private PropertiesFileEditor pfe;
    @SuppressWarnings("unused")
    private JakartaParentEditor ourEditor;
    private JakartaAllProperties appProps = null;
    private Properties jProps = null;
    
    public XSPPropBeanLoader(String namespace) {
        super(namespace);
        modList = new HashMap<String, String>();
    }
    
    public void setValue(Object instance, IAttribute attribute, String value, DataChangeNotifier dataChangeNotifier) throws NodeException {
        super.setValue(instance, attribute, value, dataChangeNotifier);
        modList.put(attribute.getName(), value);
        IDocument pfeDoc = pfe.getDocumentProvider().getDocument(fei);
        String docContents = pfeDoc.get();
        
        String keyName = appProps.keyForAttr(attribute.getName());
        docContents = XSPPropertiesUtils.instance().setPropertiesAsString(docContents, keyName, value, jProps);

        pfeDoc.set(docContents);
    }
    
    public void setPropFile(FileEditorInput attachedFile, JakartaParentEditor ourEditor) {
        fei = attachedFile;
        this.ourEditor = ourEditor;
        pfe = ourEditor.getPropertiesEditor();
        appProps = ourEditor.getDBPropObject().getXspProperties();
        jProps = appProps.getPropertiesObj();
    }

    public HashMap<String, String> getChangedSet() {
        return modList;
    }
}