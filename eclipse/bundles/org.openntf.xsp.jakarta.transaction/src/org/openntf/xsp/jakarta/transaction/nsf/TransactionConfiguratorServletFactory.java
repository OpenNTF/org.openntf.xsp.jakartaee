package org.openntf.xsp.jakarta.transaction.nsf;

import javax.servlet.ServletException;

import org.openntf.xsp.jakarta.transaction.servlet.TransactionRequestListener;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import jakarta.servlet.ServletContext;

/**
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionConfiguratorServletFactory implements IServletFactory {
	@Override
	public void init(ComponentModule module) {
		ServletContext context = ServletUtil.oldToNew(null, module.getServletContext());
		context.addListener(new TransactionRequestListener());
	}

	@Override
	public ServletMatch getServletMatch(String contextPath, String path) throws ServletException {
		// NOP
		return null;
	}

}
