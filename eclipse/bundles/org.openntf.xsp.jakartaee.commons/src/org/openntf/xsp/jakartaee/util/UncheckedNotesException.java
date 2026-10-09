package org.openntf.xsp.jakartaee.util;

import java.text.MessageFormat;

import com.ibm.domino.napi.NException;
import lotus.domino.NotesException;

/**
 * This class wraps a {@link NotesException} in a consistent way
 * to keep some of the semantic Notes meaning.
 * 
 * @since 3.8.0
 */
public class UncheckedNotesException extends RuntimeException {
	public UncheckedNotesException(NotesException e) {
		super(MessageFormat.format("{0} (Status: 0x{1})", e.text, Integer.toHexString(e.id)), e); //$NON-NLS-1$
	}
	
	public UncheckedNotesException(String message, NotesException e) {
		super(MessageFormat.format("{0} (Status: 0x{1})", message, Integer.toHexString(e.id)), e); //$NON-NLS-1$
	}
	
	public UncheckedNotesException(NException e) {
		super(MessageFormat.format("{0} (Status: 0x{1}, File: \"{2}\", Line: {3})", e.getMessage(), Integer.toHexString(e.getErrorCode()).toUpperCase(), e.getFile(), e.getLine()), e); //$NON-NLS-1$
	}
	
	public UncheckedNotesException(String message, NException e) {
		super(MessageFormat.format("{0} (Status: 0x{1}, File: \"{2}\", Line: {3})", message, Integer.toHexString(e.getErrorCode()).toUpperCase(), e.getFile(), e.getLine()), e); //$NON-NLS-1$
	}
}
