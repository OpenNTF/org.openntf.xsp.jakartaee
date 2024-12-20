package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.openntf.xsp.jakarta.nosql.communication.driver.impl.DQL.DQLTerm;

/**
 * Utility class to programmatically compose syntactically correct
 * DQL queries and prevent malicous content injection (we hopefully
 * catch all possible threats like using quote characters in search
 * values).<br>
 * <br>
 * For best code quality, use a static import like this<br>
 * <br>
 * <code>
 * import static com.mindoo.domino.jna.dql.DQL.*;
 * </code>
 * <br><br>
 * to import all static methods of this class.<br>
 * Then you can use methods like {@link #item(String)}, {@link #in(String...)},
 * {@link #inAll(String...)} or {@link #view(String)}
 * directly without the prefix <code>DQL."</code>:<br>
 * <br>
 * <code>
 * DQLTerm dqlQuery = and(<br>
 * &nbsp;&nbsp;item("Lastname").isEqualTo("Abbott"),<br>
 * &nbsp;&nbsp;item("Firstname").isGreaterThan("B")<br>
 * );<br>
 * </code>
 * The returned {@link DQLTerm} object can be passed to one of the query
 * methods in {@link NotesDatabase}, e.g. {@link NotesDatabase#query(DQLTerm, java.util.EnumSet)}
 * to receive a {@link NotesDbQueryResult} containing a {@link NotesIDTable}
 * with note ids of matching documents (and some statics like how long
 * it took to compute the result).<br>
 * See the provided sample code to find out how to project this result
 * onto a view to dynamically sort the documents.<br>
 * <br>
 * To see the resulting DQL query string, simple call {@link DQLTerm#toString()}.
 *
 * @author Karsten Lehmann
 */
public class DQL {
	/**
	 * Use this method to filter for documents with a specific item
	 * value
	 *
	 * @param itemName item name
	 * @return object to define the item value and relation (e.g. isEqual to)
	 */
	public static NamedItem item(final String itemName) {
		return new NamedItem(itemName);
	}

	/**
	 * Use this method to filter for documents with a specific view
	 * column value
	 *
	 * @param viewName view name
	 * @return object to define which column to filter
	 */
	public static NamedView view(final String viewName) {
		return new NamedView(viewName);
	}

	/**
	 * Creates a search term to find documents that exist in at least
	 * one of the specified views.
	 *
	 * @param views view names (V10.0 does not support alias names)
	 * @return DQL term
	 */
	public static InViewsOrFoldersTerm in(final String...views) {
		return new InViewsOrFoldersTerm(views, false);
	}

	/**
	 * Creates a search term to find documents that exist in multiple
	 * views
	 *
	 * @param views view names (V10.0 does not support alias names)
	 * @return DQL term
	 */
	public static InViewsOrFoldersTerm inAll(final String... views) {
		return new InViewsOrFoldersTerm(views, true);
	}

	/**
	 * Base class for a varietly of
	 * @author Karsten Lehmann
	 */
	public static abstract class DQLTerm {

		/**
		 * Returns the term content as DQL query
		 *
		 * @return DQL
		 */
		@Override
		public abstract String toString();
	}

	public static class InViewsOrFoldersTerm extends DQLTerm {
		private String[] m_viewNames;
		private boolean m_matchAll;

		private String m_toString;

		private InViewsOrFoldersTerm(final String[] viewNames, final boolean matchAll) {
			m_viewNames = viewNames;
			m_matchAll = matchAll;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				StringBuilder sb = new StringBuilder();
				sb.append("in ");
				if (m_matchAll) {
					sb.append("all ");
				}
				sb.append("(");

				for (int i=0; i<m_viewNames.length; i++) {
					if (i>0) {
						sb.append(", ");
					}
					sb.append("'");
					sb.append(escapeViewName(m_viewNames[i]));
					sb.append("'");
				}
				sb.append(")");
				m_toString = sb.toString();
			}
			return m_toString;
		}
	}

	public static class NamedView {
		private String m_viewName;

		private NamedView(final String viewName) {
			m_viewName = viewName;
		}

		public String getViewName() {
			return m_viewName;
		}

		/**
		 * Method to define the column for which we want to
		 * filter the value
		 *
		 * @param columnName view column name
		 * @return object to define the column value and relation (e.g. isEqualTo)
		 */
		public NamedViewColumn column(final String columnName) {
			return new NamedViewColumn(this, columnName);
		}

		@Override
		public String toString() {
			return m_viewName;
		}

	}

	public static class NamedViewColumn extends Subject {
		private NamedView m_view;
		private String m_columnName;

		private String m_toString;

		private NamedViewColumn(final NamedView view, final String columnName) {
			m_view = view;
			m_columnName = columnName;
		}

		public String getColumnName() {
			return m_columnName;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				m_toString = "'" + escapeViewName(m_view.getViewName())+"'."+escapeColumnName(m_columnName);
			}
			return m_toString;
		}
	}

	private enum TermRelation {
		EQUAL("="),
		LESSTHAN("<"),
		LESSTHANOREQUAL("<="),
		GREATERTHAN(">"),
		GREATERTHANOREQUAL(">="),
		IN("in"),
		INALL("in all"),
		CONTAINS("contains");

		private String m_val;

		private TermRelation(final String val) {
			m_val = val;
		}

		public String getValue() {
			return m_val;
		}
	}

	private enum SpecialValueType {
		MODIFIEDINTHISFLE("@ModifiedInThisFile"),
		DOCUMENTUNIQUEID("@DocumentUniqueID"),
		CREATED("@Created");

		private String m_value;

		private SpecialValueType(final String value) {
			m_value = value;
		}

		public String getValue() {
			return m_value;
		}
	}

	/**
	 * Returns a DQL term that matches all documents
	 *
	 * @return term
	 */
	public static DQLTerm all() {
		return new AllTerm();
	}

	/**
	 * Returns a DQL term to run a FT search on the whole note with one or multiple search words.
	 *
	 * @param values search words with optional wildcards like Lehm* or Lehman?
	 * @return term
	 *
	 * @since Domino R11
	 */
	public static DQLTerm contains(final String...values) {
		return new NoteContainsTerm(false, values);
	}

	/**
	 * Returns a DQL term to run a FT search on the whole note with one or multiple search words.<br>
	 * All search words must exist in the note for it to be a match.
	 *
	 * @param values search words with optional wildcards like Lehm* or Lehman?
	 * @return term
	 *
	 * @since Domino R11
	 */
	public static DQLTerm containsAll(final String...values) {
		return new NoteContainsTerm(true, values);
	}

	/**
	 * Returns a DQL term to do an AND operation on multiple other terms
	 *
	 * @param terms terms for AND operation
	 * @return AND term
	 */
	public static DQLTerm and(final DQLTerm... terms) {
		return new AndTerm(terms);
	}

	/**
	 * Returns a DQL term to do an OR operation on multiple other terms
	 *
	 * @param terms terms for OR operation
	 * @return OR term
	 */
	public static DQLTerm or(final DQLTerm... terms) {
		return new OrTerm(terms);
	}

	/**
	 * Returns a DQL term to negate the specified term
	 *
	 * @param term term to negate
	 * @return negated term
	 */
	public static DQLTerm not(final DQLTerm term) {
		return new NotTerm(term);
	}

	public static class AllTerm extends DQLTerm {

		@Override
		public String toString() {
			return "@all";
		}
	}

	public static class NoteContainsTerm extends DQLTerm {
		private boolean m_containsAll;
		private String[] m_values;
		private String m_toString;

		public NoteContainsTerm(final boolean containsAll, final String... values) {
			m_containsAll = containsAll;
			m_values = values;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				StringBuilder sb = new StringBuilder();

				sb.append("contains ");

				if (m_containsAll) {
					sb.append("all ");
				}

				sb.append("(");

				for (int i=0; i<m_values.length; i++) {
					if (i>0) {
						sb.append(", ");
					}
					sb.append("'");
					sb.append(escapeStringValue(m_values[i]));
					sb.append("'");
				}

				sb.append(")");
				m_toString = sb.toString();
			}
			return m_toString;
		}
	}

	public static class NotTerm extends DQLTerm {
		private DQLTerm m_term;

		private String m_toString;

		private NotTerm(final DQLTerm term) {
			m_term = term;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				if (m_term instanceof AndTerm || m_term instanceof OrTerm) {
					m_toString = "not ("+m_term.toString()+")";
				}
				else {
					m_toString = "not "+m_term.toString();
				}
			}
			return m_toString;
		}
	}

	public static class AndTerm extends DQLTerm {
		private DQLTerm[] m_terms;
		private String m_toString;

		private AndTerm(final DQLTerm[] terms) {
			if (terms==null) {
				throw new IllegalArgumentException("And arguments value is null");
			}
			if (terms.length==0) {
				throw new IllegalArgumentException("And arguments value is empty");
			}
			m_terms = terms;
		}

		@Override
		public String toString() {
			if (m_terms.length == 1) {
				return m_terms[0].toString();
			}

			if (m_toString==null){
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<m_terms.length; i++) {
					if (i>0) {
						sb.append(" and ");
					}

					if (m_terms[i] instanceof OrTerm) {
						sb.append("(");
						sb.append(m_terms[i].toString());
						sb.append(")");
					}
					else if (m_terms[i] instanceof AndTerm) {
						sb.append(m_terms[i].toString());
					}
					else {
						sb.append(m_terms[i]);
					}
				}

				m_toString = sb.toString();
			}
			return m_toString;
		}
	}

	public static class OrTerm extends DQLTerm {
		private DQLTerm[] m_terms;
		private String m_toString;

		private OrTerm(final DQLTerm[] terms) {
			if (terms==null) {
				throw new IllegalArgumentException("Or arguments value is null");
			}
			if (terms.length==0) {
				throw new IllegalArgumentException("Or arguments value is empty");
			}
			m_terms = terms;
		}

		@Override
		public String toString() {
			if (m_terms.length == 1) {
				return m_terms[0].toString();
			}

			if (m_toString==null) {
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<m_terms.length; i++) {
					if (i>0) {
						sb.append(" or ");
					}

					if (m_terms[i] instanceof OrTerm) {
						sb.append(m_terms[i].toString());
					}
					else if (m_terms[i] instanceof AndTerm) {
//						sb.append("(");
						sb.append(m_terms[i].toString());
//						sb.append(")");
					}
					else {
						sb.append(m_terms[i]);
					}
				}
				m_toString = sb.toString();
			}
			return m_toString;
		}

	}

	/**
	 * Use this method to filter by @ModifiedInThisFile value
	 *
	 * @return object to define a date value and relation (e.g. isGreaterThan / isLess)
	 */
	public static SpecialValue modifiedInThisFile() {
		return new SpecialValue(SpecialValueType.MODIFIEDINTHISFLE);
	}

	/**
	 * Use this method to filter by @DocumentUniqueId value
	 *
	 * @return object to define the UNID and isEqual relation
	 */
	public static SpecialValue documentUniqueId() {
		return new SpecialValue(SpecialValueType.DOCUMENTUNIQUEID);
	}

	/**
	 * Use this method to filter by @Created value
	 *
	 * @return object to define the creation date to compare with and relation (e.g. isGreaterThan)
	 */
	public static SpecialValue created() {
		return new SpecialValue(SpecialValueType.CREATED);
	}

	public static class SpecialValue extends NamedItem {
		private SpecialValueType m_type;

		private SpecialValue(final SpecialValueType type) {
			super(type.getValue());
			m_type = type;
		}

		public SpecialValueType getType() {
			return m_type;
		}

		@Override
		public String toString() {
			return m_type.getValue();
		}
	}

	private static class Subject {
		public ValueComparisonTerm isEqualTo(final String strVal) {
			return new ValueComparisonTerm(this, TermRelation.EQUAL, strVal);
		}

		public ValueComparisonTerm isLessThan(final String strVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHAN, strVal);
		}

		public ValueComparisonTerm isLessThanOrEqual(final String strVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, strVal);
		}

		public ValueComparisonTerm isGreaterThan(final String strVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, strVal);
		}

		public ValueComparisonTerm isGreaterThanOrEqual(final String strVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, strVal);
		}

		public ValueComparisonTerm isEqualTo(final int numVal) {
			return new ValueComparisonTerm(this, TermRelation.EQUAL, Integer.valueOf(numVal));
		}

		public ValueComparisonTerm isLessThan(final int numVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHAN, Integer.valueOf(numVal));
		}

		public ValueComparisonTerm isLessThanOrEqual(final int numVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, Integer.valueOf(numVal));
		}

		public ValueComparisonTerm isGreaterThan(final int numVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, Integer.valueOf(numVal));
		}

		public ValueComparisonTerm isGreaterThanOrEqual(final int numVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, Integer.valueOf(numVal));
		}

		public ValueComparisonTerm isEqualTo(final double numVal) {
			return new ValueComparisonTerm(this, TermRelation.EQUAL, Double.valueOf(numVal));
		}

		public ValueComparisonTerm isLessThan(final double numVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHAN, Double.valueOf(numVal));
		}

		public ValueComparisonTerm isLessThanOrEqual(final double numVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, Double.valueOf(numVal));
		}

		public ValueComparisonTerm isGreaterThan(final double numVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, Double.valueOf(numVal));
		}

		public ValueComparisonTerm isGreaterThanOrEqual(final double numVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, Double.valueOf(numVal));
		}

		/**
		 * Returns a DQL term to run a FT search on the value of an item with one or multiple search words.
		 *
		 * @param values search words with optional wildcards like Lehm* or Lehman?
		 * @return term
		 *
		 * @since Domino R11
		 */
		public ValueContainsTerm contains(final String...searchWords) {
			return new ValueContainsTerm(this, false, searchWords);
		}

		/**
		 * Returns a DQL term to run a FT search on the value of an item with one or multiple search words.<br>
		 * All search words must exist in the note for it to be a match.
		 *
		 * @param values search words with optional wildcards like Lehm* or Lehman?
		 * @return term
		 *
		 * @since Domino R11
		 */
		public ValueContainsTerm containsAll(final String...searchWords) {
			return new ValueContainsTerm(this, true, searchWords);
		}

		public <T> ValueComparisonTerm in(final Collection<T> values, final Class<T> clazz) {
			if (Integer.class == clazz) {
				int[] valuesArr = new int[values.size()];
				int idx = 0;
				for (T currVal : values) {
					valuesArr[idx++] = ((Number) currVal).intValue();
				}
				return in(valuesArr);
			}
			else if (Double.class ==clazz) {
				double[] valuesArr = new double[values.size()];
				int idx = 0;
				for (T currVal : values) {
					valuesArr[idx++] = ((Number)currVal).doubleValue();
				}
				return in(valuesArr);
			}
			else if (String.class == clazz) {
				String[] valuesArr = new String[values.size()];
				int idx = 0;
				for (T currVal : values) {
					valuesArr[idx++] = (String) currVal;
				}
				return in(valuesArr);
			}
			else {
				throw new IllegalArgumentException("Unsupported class type: "+clazz.getName()+". Try Integer, Double or String.");
			}
		}

		public ValueComparisonTerm in(final String... strValues) {
			if (strValues==null) {
				throw new IllegalArgumentException("Values ist cannot be null");
			}
			if (strValues.length==0) {
				throw new IllegalArgumentException("Values ist cannot be empty");
			}

			return new ValueComparisonTerm(this, TermRelation.IN, strValues);
		}

		public ValueComparisonTerm in(final int... intValues) {
			if (intValues==null) {
				throw new IllegalArgumentException("Values ist cannot be null");
			}
			if (intValues.length==0) {
				throw new IllegalArgumentException("Values ist cannot be empty");
			}

			return new ValueComparisonTerm(this, TermRelation.IN, intValues);
		}

		public ValueComparisonTerm in(final double... dblValues) {
			if (dblValues==null) {
				throw new IllegalArgumentException("Values ist cannot be null");
			}
			if (dblValues.length==0) {
				throw new IllegalArgumentException("Values ist cannot be empty");
			}

			return new ValueComparisonTerm(this, TermRelation.IN, dblValues);
		}

		public ValueComparisonTerm isEqualTo(final Date dtVal) {
			return new ValueComparisonTerm(this, TermRelation.EQUAL, dtVal);
		}

		public ValueComparisonTerm isLessThan(final Date dtVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHAN, dtVal);
		}

		public ValueComparisonTerm isLessThanOrEqual(final Date dtVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, dtVal);
		}

		public ValueComparisonTerm isGreaterThanOrEqual(final Date dtVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, dtVal);
		}

		public ValueComparisonTerm isGreaterThan(final Date dtVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, dtVal);
		}

		public ValueComparisonTerm isEqualTo(final Temporal dtVal) {
			return new ValueComparisonTerm(this, TermRelation.EQUAL, dtVal);
		}

		public ValueComparisonTerm isLessThan(final Temporal dtVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHAN, dtVal);
		}

		public ValueComparisonTerm isLessThanOrEqual(final Temporal dtVal) {
			return new ValueComparisonTerm(this, TermRelation.LESSTHANOREQUAL, dtVal);
		}

		public ValueComparisonTerm isGreaterThanOrEqual(final Temporal dtVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHANOREQUAL, dtVal);
		}

		public ValueComparisonTerm isGreaterThan(final Temporal dtVal) {
			return new ValueComparisonTerm(this, TermRelation.GREATERTHAN, dtVal);
		}

	}

	public static class NamedItem extends Subject {
		private String m_itemName;

		private NamedItem(final String itemName) {
			m_itemName = itemName;
		}

		public String getName() {
			return m_itemName;
		}

		@Override
		public String toString() {
			return escapeItemName(m_itemName);
		}
	}

	public static class ValueContainsTerm extends DQLTerm {
		private Subject m_subject;
		private boolean m_containsAll;
		private String[] m_values;

		private String m_toString;

		private ValueContainsTerm(final Subject item, final boolean containsAll, final String[] values) {
			m_subject = item;
			m_containsAll = containsAll;
			m_values = values;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				StringBuilder sb = new StringBuilder();

				sb.append(m_subject.toString());

				sb.append(" contains ");

				if (m_containsAll) {
					sb.append("all ");
				}

				sb.append("(");

				for (int i=0; i<m_values.length; i++) {
					if (i>0) {
						sb.append(", ");
					}
					sb.append("'");
					sb.append(escapeStringValue(m_values[i]));
					sb.append("'");
				}

				sb.append(")");

				m_toString = sb.toString();
			}
			return m_toString;
		}
	}

	public static class ValueComparisonTerm extends DQLTerm {
		private Subject m_subject;
		private TermRelation m_relation;
		private Object m_value;

		private String m_toString;

		private ValueComparisonTerm(final Subject item, final TermRelation relation, final Object value) {
			m_subject = item;
			m_relation = relation;
			m_value = value;
		}

		@Override
		public String toString() {
			if (m_toString==null) {
				StringBuilder sb = new StringBuilder();

				sb.append(m_subject.toString());

				sb.append(" ");
				sb.append(m_relation.getValue());
				sb.append(" ");

				if (m_relation == TermRelation.IN) {
					sb.append("(");
				}

				if (m_value instanceof String[] strValues) {
					for (int i=0; i<strValues.length; i++) {
						if (i>0) {
							sb.append(", ");
						}
						sb.append("'");
						sb.append(escapeStringValue(strValues[i]));
						sb.append("'");
					}
				}
				else if (m_value instanceof int[] intValues) {
					for (int i=0; i<intValues.length; i++) {
						if (i>0) {
							sb.append(", ");
						}
						sb.append(Integer.toString(intValues[i]));
					}
				}
				else if (m_value instanceof double[] dblValues) {
					for (int i=0; i<dblValues.length; i++) {
						if (i>0) {
							sb.append(", ");
						}
						sb.append(formatDoubleValue(dblValues[i]));
					}
				}
				else if (m_value instanceof Date[] dateValues) {
					for (int i=0; i<dateValues.length; i++) {
						if (i>0) {
							sb.append(", ");
						}
						sb.append(formatDateValue(dateValues[i]));
					}
				}
				else if (m_value instanceof Temporal[] temporalValues) {
					sb.append(Arrays.stream(temporalValues).map(DQL::formatTemporalValue).collect(Collectors.joining(", "))); //$NON-NLS-1$
				}
				else if (m_value instanceof String) {
					sb
					.append("'")
					.append(escapeStringValue((String) m_value))
					.append("'");
				}
				else if (m_value instanceof Integer) {
					sb.append(m_value);
				}
				else if (m_value instanceof Double) {
					sb.append(formatDoubleValue((Double)m_value));
				}
				else if (m_value instanceof Date) {
					sb.append(formatDateValue((Date) m_value));
				}
				else if (m_value instanceof Temporal) {
					sb.append(formatTemporalValue((Temporal) m_value));
				}
				else {
					throw new IllegalArgumentException("Unknown value found: "+m_value+" (type="+(m_value==null ? "null" : m_value.getClass().getName()+")"));
				}

				if (m_relation == TermRelation.IN) {
					sb.append(")");
				}

				m_toString = sb.toString();
			}
			return m_toString;
		}
	}

	private static String escapeItemName(final String itemName) {
		if (itemName.contains(" ")) {
			throw new IllegalArgumentException("Unexpected whitespace found in item name: "+itemName);
		}
		if (itemName.contains("'")) {
			throw new IllegalArgumentException("Unexpected quote character in item name: "+itemName);
		}
		if (itemName.contains("\"")) {
			throw new IllegalArgumentException("Unexpected quote character in item name: "+itemName);
		}

		return itemName;
	}

	private static String escapeViewName(final String viewName) {
		if (viewName.contains("'")) {
			throw new IllegalArgumentException("Unexpected quote character in view name: "+viewName);
		}
		if (viewName.contains("\"")) {
			throw new IllegalArgumentException("Unexpected quote character in view name: "+viewName);
		}
		return viewName.replace("\\", "\\\\");
	}

	private static String escapeColumnName(final String columnName) {
		if (columnName.contains(" ")) {
			throw new IllegalArgumentException("Unexpected whitespace found in view name: "+columnName);
		}
		if (columnName.contains("'")) {
			throw new IllegalArgumentException("Unexpected quote character in view name: "+columnName);
		}
		if (columnName.contains("\"")) {
			throw new IllegalArgumentException("Unexpected quote character in view name: "+columnName);
		}
		return columnName.replace("\\", "\\\\");
	}

	private static String escapeStringValue(final String strVal) {
		if (strVal.contains("\n")) {
			throw new IllegalArgumentException("Unexpected newline character in string value: "+strVal);
		}

		return strVal
				.replace("'", "''");
	}

	private static String formatDoubleValue(final double dblValue) {
		return Double.toString(dblValue);
	}

	private static ThreadLocal<DateTimeFormatter> RFC3339_PATTERN_DATETIME = ThreadLocal.withInitial(() -> DateTimeFormatter.ISO_DATE_TIME);

	private static String formatDateValue(final Date dateValue) {
		//Domino can only store hundredth of a second
		long dateValueMS = dateValue.getTime();
		long millis = dateValue.getTime() % 1000;
		long millisRounded = 10 * (millis / 10);
		dateValueMS -= millis-millisRounded;

		return MessageFormat.format("@dt(''{0}'')", DQL.RFC3339_PATTERN_DATETIME.get().format(OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateValueMS), ZoneId.of("UTC")))); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String formatTemporalValue(final Temporal temporalValue) {
		if(temporalValue instanceof LocalDate) {
			return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_LOCAL_DATE.format(temporalValue)); //$NON-NLS-1$
		} else if(temporalValue instanceof LocalTime) {
			return MessageFormat.format("@dt(''{0}'')", DateTimeFormatter.ISO_LOCAL_TIME.format(temporalValue)); //$NON-NLS-1$
		} else {
			// Treat it as an instant
			Instant inst = Instant.from(temporalValue);
			return formatDateValue(Date.from(inst));
		}
	}
}