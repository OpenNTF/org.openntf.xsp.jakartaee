package org.openntf.xsp.jakartaee.module.nsf;

import jakarta.servlet.http.HttpServletRequest;

public record ActiveRequest(NSFJakartaModule module, HttpServletRequest request) {

}
