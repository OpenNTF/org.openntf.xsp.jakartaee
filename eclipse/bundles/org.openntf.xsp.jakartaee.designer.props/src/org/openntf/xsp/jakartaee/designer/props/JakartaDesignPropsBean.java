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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;

public class JakartaDesignPropsBean {
    private IFile propsFile;
    private DesignerProject project;
    private Properties props;
    
    public JakartaDesignPropsBean(DesignerProject desPrj, IFile file) {
        propsFile = file;
        project = desPrj;
        props = new Properties();
    }
    
    public void save(IProgressMonitor monitor) {
        if(propsFile == null){
            if(project != null){
                IProject prj = project.getProject();
                propsFile = prj.getFile("AppProperties/xspdesign.properties"); //$NON-NLS-1$
            }
        }
        ByteStreamCache cache = new ByteStreamCache();
        OutputStream os = cache.getOutputStream();
        try {
            props.store(os, null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        InputStream is = cache.getInputStream();
        if(propsFile != null){
            try {
                if(!propsFile.exists()){

                    propsFile.create(is, true, monitor);
                }
                else{
                    propsFile.setContents(cache.getInputStream(), true, true, monitor);

                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
            finally{
                if(is != null){
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }       
    }
}