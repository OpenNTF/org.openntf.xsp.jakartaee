package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.util.Optional;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;

import jakarta.servlet.http.HttpServletRequest;

public record ActiveRequest(NSFJakartaModule module, LSXBEHolder lsxbe, HttpServletRequest request) {

	private static ThreadLocal<ActiveRequest> ACTIVE_REQUEST = new ThreadLocal<>();
	
	public static Optional<ActiveRequest> get() {
		return Optional.ofNullable(ACTIVE_REQUEST.get());
	}
	public static void set(ActiveRequest request) {
		ACTIVE_REQUEST.set(request);
	}
	public static void pushRequest(HttpServletRequest request) {
		ActiveRequest active = get().map(req -> req.withRequest(request)).orElse(null);
		set(active);
	}
	
	public ActiveRequest withRequest(HttpServletRequest request) {
		return new ActiveRequest(module, lsxbe, request);
	}
}
