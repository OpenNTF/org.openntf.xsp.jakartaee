package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.text.MessageFormat;

import lotus.domino.NotesException;

public class UncheckedNotesException extends RuntimeException {
	public UncheckedNotesException(NotesException e) {
		super(MessageFormat.format("{0} (0x{1})", e.text, Integer.toHexString(e.id).toUpperCase()), e);
	}
}
