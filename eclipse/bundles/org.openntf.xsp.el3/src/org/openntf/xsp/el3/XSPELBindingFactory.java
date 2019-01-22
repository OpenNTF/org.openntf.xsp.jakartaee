package org.openntf.xsp.el3;

import javax.faces.application.Application;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.binding.BindingFactory;
import com.sun.faces.el.MethodBindingImpl;
import com.sun.faces.el.ValueBindingImpl;
import com.sun.faces.util.Util;

public class XSPELBindingFactory implements BindingFactory {

	public static final String IBM_PREFIX = "xspel";
	
	private final String prefix;
	
	public XSPELBindingFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public ValueBinding createValueBinding(Application application, String ref) {
		ValueBindingImpl result = new ValueBindingImpl(application);
		result.setRef(Util.stripBracketsIfNecessary(cleanRef(ref)));
		return result;
	}

	@Override
	public MethodBinding createMethodBinding(Application application, String ref, @SuppressWarnings("rawtypes") Class[] args) {
		return new MethodBindingImpl(application, cleanRef(ref), args);
	}

	private String cleanRef(String ref) {
		if(ref.startsWith("#{" + prefix + ':')) {
			return ref.substring(0, 2) + ref.substring(prefix.length()+3);
		} else {
			return ref;
		}
	}
}
