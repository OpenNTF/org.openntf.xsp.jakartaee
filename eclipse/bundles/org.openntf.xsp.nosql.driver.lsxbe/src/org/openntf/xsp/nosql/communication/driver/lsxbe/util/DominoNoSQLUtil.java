/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;

import jakarta.nosql.mapping.Column;
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
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 */
	public static Path getTempDirectory() {
		String osName = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("os.name")); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			String tempDir = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
			return Paths.get(tempDir);
		}
	}

	public static Object toDominoFriendly(Session session, Object value) throws NotesException {
		if(value instanceof Iterable) {
			Vector<Object> result = new Vector<Object>();
			for(Object val : (Iterable<?>)value) {
				result.add(toDominoFriendly(session, val));
			}
			return result;
		} else if(value instanceof Date) {
			return session.createDateTime((Date)value);
		} else if(value instanceof Calendar) {
			return session.createDateTime((Calendar)value);
		} else if(value instanceof Number) {
			return ((Number)value).doubleValue();
		} else if(value instanceof Boolean) {
			// TODO figure out if this can be customized, perhaps from the Settings element
			return (Boolean)value ? "Y": "N"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if(value instanceof LocalDate) {
			// TODO fix these Temporals when the API improves
			Instant inst = ZonedDateTime.of((LocalDate)value, LocalTime.of(12, 0), ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyTime();
			return dt;
		} else if(value instanceof LocalTime) {
			Instant inst = ZonedDateTime.of(LocalDate.now(), (LocalTime)value, ZoneId.systemDefault()).toInstant();
			DateTime dt = session.createDateTime(Date.from(inst));
			dt.setAnyDate();
			return dt;
		} else if(value instanceof TemporalAccessor) {
			Instant inst = Instant.from((TemporalAccessor)value);
			DateTime dt = session.createDateTime(Date.from(inst));
			return dt;
		} else {
			// TODO support other types above
			return value.toString();
		}
	}
	
	private static final String ITEM_TEMPTIME = "$$TempTime"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISODATE = "m := @Month($$TempTime);\n"
		+ "d := @Day($$TempTime);\n"
		+ "@Text(@Year($$TempTime)) + \"-\" + @If(m < 10; \"0\"; \"\") + @Text(m) + \"-\" + @If(d < 10; \"0\"; \"\") + @Text(d)";
	@SuppressWarnings("nls")
	private static final String FORMULA_TOISOTIME = "h := @Hour($$TempTime);\n"
		+ "m := @Minute($$TempTime);\n"
		+ "s := @Second($$TempTime);\n"
		+ "@If(h < 10; \"0\"; \"\") + @Text(h) + \":\" + @If(m < 10; \"0\"; \"\") + @Text(m) + \":\" + @If(s < 10; \"0\"; \"\") + @Text(s)";
	
	/**
	 * Converts the provided value read from Domino to a stock JDK type, if necessary.
	 * 
	 * @param value the value to convert
	 * @return a stock-JDK object representing the value
	 */
	public static Object toJavaFriendly(lotus.domino.Database context, Object value) {
		if(value instanceof Iterable) {
			return StreamSupport.stream(((Iterable<?>)value).spliterator(), false)
				.map(val -> toJavaFriendly(context, val))
				.collect(Collectors.toList());
		} else if(value instanceof DateTime) {
			// TODO improve with a better API
			try {
				DateTime dt = (DateTime)value;
				return toTemporal(context, dt);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else if(value instanceof DateRange) {
			try {
				DateRange dr = (DateRange)value;
				Temporal start = (Temporal)DominoNoSQLUtil.toDominoFriendly(context.getParent(), dr.getStartDateTime());
				Temporal end = (Temporal)DominoNoSQLUtil.toDominoFriendly(context.getParent(), dr.getEndDateTime());
				return Arrays.asList(start, end);
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		} else {
			// String, Double
			return value;
		}
	}

	public static Temporal toTemporal(Database context, DateTime dt) throws NotesException {
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

	public static InputStream wrapInputStream(InputStream is, String encoding) throws IOException {
		if("gzip".equals(encoding)) { //$NON-NLS-1$
			return new GZIPInputStream(is);
		} else if(encoding == null || encoding.isEmpty()) {
			return is;
		} else {
			throw new UnsupportedOperationException("Unsupported MIMEBean encoding: " + encoding);
		}
	}

	public static boolean isValid(lotus.domino.Document doc) {
		try {
			return doc != null && doc.isValid() && !doc.isDeleted() && doc.getCreated() != null;
		} catch (NotesException e) {
			return false;
		}
	}
	
	/**
	 * Determines the back-end item name for the given Java property.
	 * 
	 * @param propName the Java property to check
	 * @param mapping the {@link ClassMapping} instance for the class in question
	 * @return the effective item name based on the class properties
	 */
	public static String findItemName(String propName, ClassMapping mapping) {
		if(mapping != null) {
			Column annotation = mapping.getFieldMapping(propName)
				.map(FieldMapping::getNativeField)
				.map(f -> f.getAnnotation(Column.class))
				.orElse(null);
			if(annotation != null && !annotation.value().isEmpty()) {
				return annotation.value();
			} else {
				return propName;
			}
		} else {
			return propName;
		}
	}
}
