package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import java.text.MessageFormat;

import com.ibm.designer.domino.napi.NotesAPIException;

import lotus.domino.NotesException;

/**
 * This class wraps a {@link NotesException} in a consistent way
 * to keep some of the semantic Notes meaning.
 * 
 * @since 3.8.0
 */
public class UncheckedNotesException extends RuntimeException {
	public UncheckedNotesException(NotesException e) {
		super(MessageFormat.format("{0} (Status: 0x{1})", e.text, Integer.toHexString(e.id).toUpperCase()), e); //$NON-NLS-1$
	}
	
	public UncheckedNotesException(NotesAPIException e) {
		super(MessageFormat.format("{0} (Status: 0x{1}, File: \"{2}\", Line: {3})", e.getMessage(), Integer.toHexString(e.getNativeErrorCode()).toUpperCase(), e.getNativeFile(), e.getNativeLine()), e); //$NON-NLS-1$
	}
}
