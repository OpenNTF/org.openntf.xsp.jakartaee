package org.openntf.xsp.jakartaee.util;

import java.io.IOException;
import java.io.PrintWriter;

import com.ibm.commons.util.HtmlCommonsUtil;
import com.ibm.designer.runtime.domino.adapter.ResourceHandler;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

public enum JakartaErrorPage {
	;

	public static void handlePageNotFound(PrintWriter pw, String url, Exception cause,
			String lang, boolean rtlLang) throws IOException {
		String str = ResourceHandler.getString("XSPErrorPage.PageNotFound"); //$NON-NLS-1$
		XSPErrorPage.writeErrorPageHeader(pw, str, lang, rtlLang);
		pw.println("<h1>" + str + "</h1>");
		if (url != null) {
			pw.println("<span class=\"row\"><b>URL: </b>" + HtmlCommonsUtil.toXhtml(url) + "</span>");
		}

		if (cause != null) {
			XSPErrorPage.writeExceptionMessage(pw, cause);
			XSPErrorPage.writeExceptionTrace(pw, cause, rtlLang);
		}

		XSPErrorPage.writeErrorPageFooter(pw);

	}
}
