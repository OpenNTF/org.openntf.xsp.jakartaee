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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.FileEditorInput;
import org.openntf.xsp.jakartaee.designer.props.editor.JakartaParentEditor;

import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;

/**
 * @author mgl
 *
 */
public class JakartaPropBean {
    private DesignerProject desProject;
    private JakartaAllProperties xspProps;
    private XSPPropBeanLoader dbLoader;
    private IFile xspPropFile = null;
    private JakartaDesignPropsBean xspDesignProps;
    
    public JakartaPropBean(DesignerProject desPrj, XSPPropBeanLoader dbl, JakartaParentEditor dbEditor, FileEditorInput fei, JakartaDesignPropsBean designProps) {
        this.desProject = desPrj;
        dbLoader = dbl;
        xspPropFile = fei.getFile();
        xspDesignProps = designProps;
    }
    
    // we're all done, can let it go
    public void release() {
    }

    @Override
    protected void finalize() throws Throwable {
        release();
    }
    
    public JakartaAllProperties getXspProperties() {
        if (xspProps == null) {
            xspProps = new JakartaAllProperties(desProject, xspPropFile);
        }
        return xspProps;
    }
    
    public void setXspProperties(JakartaAllProperties allProps){
        this.xspProps = allProps;
    }
    
    public void save(IProgressMonitor monitor) {
        HashMap<String, String> al = dbLoader.getChangedSet();
        // clear the changed set, as we've saved it all now
        al.clear();
    }

    /**
     * @return the xspDesignPropsBean
     */
    public JakartaDesignPropsBean getXspDesignProps() {
        return xspDesignProps;
    }

    /**
     * @param xspDesignPropsBean the xspDesignPropsBean to set
     */
    public void setXspDesignProps(JakartaDesignPropsBean xspDesignProps) {
        this.xspDesignProps = xspDesignProps;
    }

    /**
     * @return the desProject
     */
    public DesignerProject getDesProject() {
        return desProject;
    }

    /**
     * @param desProject the desProject to set
     */
    public void setDesProject(DesignerProject desProject) {
        this.desProject = desProject;
    }
}