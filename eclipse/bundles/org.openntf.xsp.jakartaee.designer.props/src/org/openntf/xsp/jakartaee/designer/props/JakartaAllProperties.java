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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.designer.domino.ide.resources.extensions.DesignerProject;


/**
 * @author mleland
 *
 */
public class JakartaAllProperties implements JakartaAllPropertyConstants {
    private Properties ourProps = null;
    private IFile ourPropsFile = null;
    private static final HashMap<String, String> keyAttrMap = new HashMap<String, String>(40);
    
    public JakartaAllProperties(DesignerProject project, IFile ourFile){
        ourPropsFile = ourFile;
        try(InputStream propIS = ourPropsFile.exists() ? ourPropsFile.getContents() : getDefaultContent()) {
            this.ourProps = new Properties();
            if (propIS !=null) {
                // Read the current file if it exists
            	this.ourProps.load(propIS);
            }
        } catch(IOException e) {
        	e.printStackTrace();
        } catch (CoreException e1) {
			e1.printStackTrace();
		}
        
        initAttrKeyMap();
    }
    
    public Properties getPropertiesObj() {
        return ourProps;
    }
    
    private InputStream getDefaultContent() {
        Properties defProps = new Properties();
        ByteStreamCache bsc = new ByteStreamCache();
        try {
            defProps.store(bsc.getOutputStream(), ""); //$NON-NLS-1$
        } catch (IOException e) {
            return null;
        }
        return bsc.getInputStream();
    }
    
    // *******************************************************************************
	// * Property getters and setters 
	// *******************************************************************************
    
    public void setElPrefix(String prefix) {
    	firePropertyChange("elPrefix", ourProps.put(EL_PREFIX, prefix), prefix); //$NON-NLS-1$
    }
    public String getElPrefix() {
    	return (String)ourProps.getOrDefault(EL_PREFIX, ""); //$NON-NLS-1$
    }
    
    public void setRestBasePath(String path) {
    	firePropertyChange("restBasePath", ourProps.put(REST_PATH, path), path); //$NON-NLS-1$
    }
    public String getRestBasePath() {
    	return (String)ourProps.getOrDefault(REST_PATH, ""); //$NON-NLS-1$
    }
    
    public void setCorsEnable(String corsEnable) {
    	firePropertyChange("corsEnable", ourProps.put(CORS_ENABLE, corsEnable), corsEnable); //$NON-NLS-1$
    }
    public String getCorsEnable() {
    	return (String)ourProps.getOrDefault(CORS_ENABLE, Boolean.toString(false));
    }
    public void setCorsAllowCredentials(String corsAllowCredentials) {
    	firePropertyChange("corsAllowCredentials", ourProps.put(CORS_ALLOW_CREDENTIALS, corsAllowCredentials), corsAllowCredentials); //$NON-NLS-1$
    }
    public String getCorsAllowCredentials() {
    	return (String)ourProps.getOrDefault(CORS_ALLOW_CREDENTIALS, Boolean.toString(true));
    }
    public void setCorsAllowedMethods(String corsAllowedMethods) {
    	firePropertyChange("corsAllowedMethods", ourProps.put(CORS_ALLOWED_METHODS, corsAllowedMethods), corsAllowedMethods); //$NON-NLS-1$
    }
    public String getCorsAllowedMethods() {
    	return (String)ourProps.getOrDefault(CORS_ALLOWED_METHODS, ""); //$NON-NLS-1$
    }
    public void setCorsAllowedHeaders(String corsAllowedHeaders) {
    	firePropertyChange("corsAllowedHeaders", ourProps.put(CORS_ALLOWED_HEADERS, corsAllowedHeaders), corsAllowedHeaders); //$NON-NLS-1$
    }
    public String getCorsAllowedHeaders() {
    	return (String)ourProps.getOrDefault(CORS_ALLOWED_HEADERS, ""); //$NON-NLS-1$
    }
    public void setCorsExposedHeaders(String corsExposedHeaders) {
    	firePropertyChange("corsExposedHeaders", ourProps.put(CORS_EXPOSED_HEADERS, corsExposedHeaders), corsExposedHeaders); //$NON-NLS-1$
    }
    public String getCorsExposedHeaders() {
    	return (String)ourProps.getOrDefault(CORS_EXPOSED_HEADERS, ""); //$NON-NLS-1$
    }
    public void setCorsMaxAge(String corsMaxAge) {
    	firePropertyChange("corsMaxAge", ourProps.put(CORS_MAX_AGE, corsMaxAge), corsMaxAge); //$NON-NLS-1$
    }
    public String getCorsMaxAge() {
    	return (String)ourProps.getOrDefault(CORS_MAX_AGE, String.valueOf(600));
    }
    public void setCorsAllowedOrigins(String corsAllowedOrigins) {
    	firePropertyChange("corsAllowedOrigins", ourProps.put(CORS_ALLOWED_ORIGINS, corsAllowedOrigins), corsAllowedOrigins); //$NON-NLS-1$
    }
    public String getCorsAllowedOrigins() {
    	return (String)ourProps.getOrDefault(CORS_ALLOWED_ORIGINS, ""); //$NON-NLS-1$
    }
    
    public void setFacesProjectStage(String facesProjectStage) {
    	firePropertyChange("facesProjectStage", ourProps.put(FACES_PROJECT_STAGE, facesProjectStage), facesProjectStage); //$NON-NLS-1$
    }
    public String getFacesProjectStage() {
    	return (String)ourProps.getOrDefault(FACES_PROJECT_STAGE, ""); //$NON-NLS-1$
    }
    
    // *******************************************************************************
	// * Property change support
	// *******************************************************************************
    
	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
    
    // *******************************************************************************
	// * Reflective access
	// *******************************************************************************
    
    private void initAttrKeyMap() {
        keyAttrMap.put(EL_PREFIX, "elPrefix"); //$NON-NLS-1$
        
        keyAttrMap.put(REST_PATH, "restBasePath"); //$NON-NLS-1$
        
        keyAttrMap.put(CORS_ENABLE, "corsEnable"); //$NON-NLS-1$
        keyAttrMap.put(CORS_ALLOW_CREDENTIALS, "corsAllowCredentials"); //$NON-NLS-1$
        keyAttrMap.put(CORS_ALLOWED_METHODS, "corsAllowedMethods"); //$NON-NLS-1$
        keyAttrMap.put(CORS_ALLOWED_HEADERS, "corsAllowedHeaders"); //$NON-NLS-1$
        keyAttrMap.put(CORS_EXPOSED_HEADERS, "corsExposedHeaders"); //$NON-NLS-1$
        keyAttrMap.put(CORS_MAX_AGE, "corsMaxAge"); //$NON-NLS-1$
        keyAttrMap.put(CORS_ALLOWED_ORIGINS, "corsAllowedOrigins"); //$NON-NLS-1$
        
        keyAttrMap.put(FACES_PROJECT_STAGE, "facesProjectStage"); //$NON-NLS-1$
    }
    
    // really clunky, but need a bean to do the binding!
    public String keyForAttr(String attrName) {
        Set<Entry<String, String>> entrySet = keyAttrMap.entrySet();
        Iterator<Entry<String, String>> it = entrySet.iterator();
        while(it.hasNext()) {
            Entry<String, String> one = it.next();
            if (StringUtil.equals(attrName, one.getValue()))
                return one.getKey();
        }
        return null;
    }
}