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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;
import com.ibm.designer.domino.ide.resources.metamodel.IDesignElementExtension;
import com.ibm.designer.domino.ui.commons.extensions.DesignerFileEditorInput;

public class XSPPropertiesMetaModel extends IDesignElementExtension {

    @Override
    public String getLargeIcon() {
        return "jakartaProp_large.png"; //$NON-NLS-1$
    }

    @Override
    public String getSmallIcon() {
        return "jakartaProp.png"; //$NON-NLS-1$
    }

    public String getElementNameWithAccelerator() {
        return "Jakarta EE Properties";
    }

    @Override
    public ImageDescriptor getImageDescriptor(String imageName) {
        return Activator.getImageDescriptor(imageName);
    }

    @Override
    public boolean openDesign(DesignerProject designerProject) {
        if(designerProject != null){
            IProject project = designerProject.getProject();
            if(project != null){
                
                IFile xspDotProps = project.getFile("WebContent/WEB-INF/xsp.properties"); //$NON-NLS-1$
                if(xspDotProps != null && xspDotProps.exists()){
                    if(PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null){
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        if(page != null){
                            try {
                                page.openEditor(new DesignerFileEditorInput(xspDotProps), "org.openntf.xsp.jakartaee.designer.props.editor"); //$NON-NLS-1$
                                return true;
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String[] getSupportedPerspectives() {
        return new String[] {DD_PERSPECTIVE, XPAGES_PERSPECTIVE};
    }
    
    // *******************************************************************************
	// * NOP stubs
	// *******************************************************************************

    @Override
    public String getNewDialogTitle() {
        return null;
    }

    @Override
    public String getNewDialogMessage() {
        return null;
    }

    @Override
    public String getNewDialogImageName() {
        return null;
    }

    @Override
    public String getTopContextNewMenuString() {
        return null;
    }

    @Override
    public String getNewMenuStringWithAccelerator() {
        return null;
    }

    @Override
    public String getNewActionButtonLabel() {
        return null;
    }

    @Override
    public String getNewActionButtonTooltip() {
        return null;
    }

    @Override
    public String getNewActionButtonImage() {
        return null;
    }

    @Override
    public Image getImage(String imageName) {
        return null;
    }
}