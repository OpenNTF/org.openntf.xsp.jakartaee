/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.extlib;

import com.ibm.xsp.extlib.beans.DeviceBean;
import com.ibm.xsp.extlib.beans.PeopleBean;
import com.ibm.xsp.extlib.beans.UserBean;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import javax.faces.context.FacesContext;

/**
 * Contributes known XPages Extension Library beans when the ExtLib
 * is active for the current application.
 * 
 * @since 2.13.0
 */
@ApplicationScoped
public class ExtLibBeanProvider {
	@Produces
	@Named(UserBean.BEAN_NAME)
	@Dependent
	public UserBean getUserBean() {
		if(FacesContext.getCurrentInstance() == null) {
			return null;
		}
		return UserBean.get();
	}
	
	@Produces
	@Named(PeopleBean.BEAN_NAME)
	@Dependent
	public PeopleBean getPeopleBean() {
		if(FacesContext.getCurrentInstance() == null) {
			return null;
		}
		return PeopleBean.get();
	}
	
	@Produces
	@Named("deviceBean")
	@Dependent
	public DeviceBean getDeviceBean() {
		if(FacesContext.getCurrentInstance() == null) {
			return null;
		}
		return (DeviceBean)ExtLibUtil.resolveVariable("deviceBean"); //$NON-NLS-1$
	}
}
