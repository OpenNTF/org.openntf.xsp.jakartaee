package org.openntf.xsp.cdi.extlib;

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
