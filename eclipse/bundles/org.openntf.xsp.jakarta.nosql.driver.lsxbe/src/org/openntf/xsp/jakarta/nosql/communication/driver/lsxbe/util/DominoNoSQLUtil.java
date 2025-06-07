/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.BooleanStorage;

import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.DateTime;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Contains utility methods for working with files
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public enum DominoNoSQLUtil {
	;

	/**
	 * Optional setting for a directory to be returned by {@link #getTempDirectory()} instead
	 * of the system default.
	 * @since 3.1.0
	 */
	private static Path OVERRIDE_TEMP_DIR;

	/**
	 * Optional setting for a directory to be returned by {@link #getTQrpDirectory()} instead
	 * of the system default.
	 * @since 3.1.0
	 */
	private static Path OVERRIDE_QRP_DIR;

	/**
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 */
	public static Path getTempDirectory() {
		Path override = OVERRIDE_TEMP_DIR;
		if(override != null) {
			return override;
		}

		String osName = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("os.name")); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			String tempDir = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
			return Paths.get(tempDir);
		}
	}

	public static Object toDominoFriendly(final Session session, final Object value, final Optional<BooleanStorage> optBoolean) throws NotesException {
		if(value instanceof Iterable it) {
			Vector<Object> result = new Vector<>();
			for(Object val : it) {
				result.add(toDominoFriendly(session, val, optBoolean));
			}
			return result;
		} else if(value instanceof Date d) {
			return session.createDateTime(d);
		} else if(value instanceof Calendar c) {
			return session.createDateTime(c);
		} else if(value instanceof Number n) {
			return n.doubleValue();
		} else if(value instanceof Boolean b) {
			if(optBoolean.isPresent()) {
				return switch (optBoolean.get().type()) {
					case DOUBLE -> b ? optBoolean.get().doubleTrue() : optBoolean.get().doubleFalse();
					case STRING -> b ? optBoolean.get().stringTrue() : optBoolean.get().stringFalse();
					default -> b ? optBoolean.get().stringTrue() : optBoolean.get().stringFalse();
				};
			}
			return (Boolean)value ? "Y": "N"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if(value instanceof LocalDate d) {
			// TODO fix these Temporals when the API improves
			Instant inst = ZonedDateTime.of(d, LocalTime.of(12, 0), ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyTime();
			return dt;
		} else if(value instanceof LocalTime t) {
			Instant inst = ZonedDateTime.of(LocalDate.now(), t, ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyDate();
			return dt;
		} else if(value instanceof TemporalAccessor t) {
			Instant inst = Instant.from(t);
			DateTime dt = session.createDateTime(Date.from(inst));
			return dt;
		} else if(value == null) {
			return null;
		} else {
			// TODO support other types above
			return value.toString();
		}
	}

	private static final String ITEM_TEMPTIME = "$$TempTime"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISODATE = """
		m := @Month($$TempTime);
		d := @Day($$TempTime);
		@Text(@Year($$TempTime)) + "-" + @If(m < 10; "0"; "") + @Text(m) + "-" + @If(d < 10; "0"; "") + @Text(d)""";
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISOTIME = """
		h := @Hour($$TempTime);
		m := @Minute($$TempTime);
		s := @Second($$TempTime);
		@If(h < 10; "0"; "") + @Text(h) + ":" + @If(m < 10; "0"; "") + @Text(m) + ":" + @If(s < 10; "0"; "") + @Text(s)""";

	/**
	 * Converts the provided value read from Domino to a stock JDK type, if necessary.
	 *
	 * @param value the value to convert
	 * @return a stock-JDK object representing the value
	 */
	@SuppressWarnings("unchecked")
	public static Object toJavaFriendly(final lotus.domino.Database context, final Object value, final Optional<BooleanStorage> optBoolean) {
		if(value instanceof Iterable i) {
			return StreamSupport.stream(i.spliterator(), false)
				.map(val -> toJavaFriendly(context, val, optBoolean))
				.collect(Collectors.toList());
		} else if(value instanceof DateTime dt) {
			// TODO improve with a better API
			try {
				return toTemporal(context, dt);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if(value instanceof DateRange dr) {
			try {
				Temporal start = (Temporal)DominoNoSQLUtil.toDominoFriendly(context.getParent(), dr.getStartDateTime(), optBoolean);
				Temporal end = (Temporal)DominoNoSQLUtil.toDominoFriendly(context.getParent(), dr.getEndDateTime(), optBoolean);
				return Arrays.asList(start, end);
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		} else if(value instanceof Number n) {
			if(optBoolean.isPresent()) {
				if(optBoolean.get().type() == BooleanStorage.Type.DOUBLE) {
					return n.doubleValue() == optBoolean.get().doubleTrue();
				} else {
					return false;
				}
			} else {
				return value;
			}
		} else {
			// String
			if(optBoolean.isPresent()) {
				if(optBoolean.get().type() == BooleanStorage.Type.STRING) {
					return optBoolean.get().stringTrue().equals(value);
				} else {
					return false;
				}
			} else {
				return value;
			}
		}
	}

	public static Temporal toTemporal(final Database context, final DateTime dt) throws NotesException {
		try {
			String datePart = dt.getDateOnly();
			String timePart = dt.getTimeOnly();
			if(datePart == null || datePart.isEmpty()) {
				lotus.domino.Document tempDoc = context.createDocument();
				tempDoc.replaceItemValue(ITEM_TEMPTIME, dt);
				String iso = (String)dt.getParent().evaluate(FORMULA_TOISOTIME, tempDoc).get(0);
				Instant inst = dt.toJavaDate().toInstant();
				int nano = inst.getNano();
				iso += "." + nano; //$NON-NLS-1$
				return LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(iso));
			} else if(timePart == null || timePart.isEmpty()) {
				lotus.domino.Document tempDoc = context.createDocument();
				tempDoc.replaceItemValue(ITEM_TEMPTIME, dt);
				String iso = (String)dt.getParent().evaluate(FORMULA_TOISODATE, tempDoc).get(0);
				return LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(iso));
			} else {
				return dt.toJavaDate().toInstant();
			}
		} finally {
			dt.recycle();
		}
	}

	public static DateTime fromTemporal(final Session session, final TemporalAccessor time) throws NotesException {
		try {
			Instant inst = Instant.from(time);
			return session.createDateTime(Date.from(inst));
		} catch(DateTimeException e) {
		}
		try {
			OffsetDateTime dt = OffsetDateTime.from(time);
			return session.createDateTime(Date.from(dt.toInstant()));
		} catch(DateTimeException e) {
		}
		try {
			ZonedDateTime dt = ZonedDateTime.from(time);
			return session.createDateTime(Date.from(dt.toInstant()));
		} catch(DateTimeException e) {
		}
		try {
			LocalDate localDate = LocalDate.from(time);
			Date date = Date.from(ZonedDateTime.of(localDate, LocalTime.now(), ZoneId.systemDefault()).toInstant());
			DateTime dt = session.createDateTime(date);
			dt.setAnyTime();
			return dt;
		} catch(DateTimeException e) {
		}
		try {
			LocalTime localTime = LocalTime.from(time);
			Date date = Date.from(ZonedDateTime.of(LocalDate.now(), localTime, ZoneId.systemDefault()).toInstant());
			DateTime dt = session.createDateTime(date);
			dt.setAnyDate();
			return dt;
		} catch(DateTimeException e) {
		}
		throw new IllegalArgumentException(MessageFormat.format("Unsupported time: {0} (class {1})", time, time == null ? null : time.getClass().getName()));
	}

	public static InputStream wrapInputStream(final InputStream is, final String encoding) throws IOException {
		if("gzip".equals(encoding)) { //$NON-NLS-1$
			return new GZIPInputStream(is);
		} else if(encoding == null || encoding.isEmpty()) {
			return is;
		} else {
			throw new UnsupportedOperationException("Unsupported MIMEBean encoding: " + encoding);
		}
	}

	public static boolean isValid(final lotus.domino.Document doc) {
		try {
			return doc != null && doc.isValid() && !doc.isDeleted() && doc.getCreated() != null;
		} catch (NotesException e) {
			return false;
		}
	}

	/**
	 * Sets the directory to be used for temp files, instead of the system default.
	 *
	 * <p>This path must be on the primary local filesystem.</p>
	 *
	 * @param dir a local-filesystem path to use, or {@code null} to un-set an
	 *        existing value
	 * @since 3.1.0
	 */
	public static void setTempDirectory(final Path dir) {
		OVERRIDE_TEMP_DIR = dir;
	}

	/**
	 * Sets the directory to be used for QRP databases, instead of the system default.
	 *
	 * <p>This path must be on the primary local filesystem.</p>
	 *
	 * @param dir a local-filesystem path to use, or {@code null} to un-set an
	 *        existing value
	 * @since 3.1.0
	 */
	public static void setQrpDirectory(final Path dir) {
		OVERRIDE_QRP_DIR = dir;
	}

	/**
	 * Retrieves the base directory to be used for QRP databases
	 *
	 * @return a path to use for QRP databases, or an empty Optional if this
	 *         was not set
	 * @since 3.1.0
	 */
	public static Optional<Path> getQrpDirectory() {
		return Optional.ofNullable(OVERRIDE_QRP_DIR);
	}
}
