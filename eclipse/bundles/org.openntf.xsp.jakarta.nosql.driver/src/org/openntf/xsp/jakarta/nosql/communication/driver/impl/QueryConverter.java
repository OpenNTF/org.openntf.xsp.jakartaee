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
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.lang.reflect.Array;
import java.time.temporal.Temporal;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.jakarta.nosql.communication.driver.impl.DQL.DQLTerm;

/**
 * Assistant class to convert queries from Diana internal structures to DQL queries
 *
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public enum QueryConverter {
	;

	private static final String[] ALL_SELECT = { "*" }; //$NON-NLS-1$

	private static final String[] EMPTY_STRING_ARRAY = {};

	public static QueryConverterResult select(final SelectQuery query, final EntityMetadata mapping) {
		String[] documents = query.columns().toArray(new String[0]);
		if (documents.length == 0) {
			documents = ALL_SELECT;
		}

		DQLTerm statement;
		long skip = query.skip();
		long limit = query.limit();

		String formName = EntityUtil.getFormName(mapping);

		if (query.condition().isPresent()) {
			statement = getCondition(query.condition().get());
			// Add in the form property if needed
			statement = applyFormName(statement, formName);
		} else {
			statement = applyFormName(null, formName);
		}
		return new QueryConverterResult(documents, statement, skip, limit);
	}

	public static DQLTerm getCondition(final CriteriaCondition condition) {
		Element document = condition.element();

		// Convert special names
		String name = String.valueOf(document.name());
		switch (name) {
			case DominoConstants.FIELD_ID:
				name = "@DocumentUniqueID"; //$NON-NLS-1$
				break;
			case DominoConstants.FIELD_CDATE:
				name = "@Created"; //$NON-NLS-1$
				break;
			case DominoConstants.FIELD_MDATE:
				name = "@Modified"; //$NON-NLS-1$
				break;
			default:
				if("$REF".equalsIgnoreCase(name)) { //$NON-NLS-1$
					name = "@Text($REF)"; //$NON-NLS-1$
				} else if(DominoConstants.FIELD_SIZE.equals(name)) {
					name = "@DocLength"; //$NON-NLS-1$
				} else if(DominoConstants.FIELD_NOTEID.equals(name)) {
					name = "@NoteID"; //$NON-NLS-1$
				} else if(DominoConstants.FIELD_ADATE.equals(name)) {
					name = "@Accessed"; //$NON-NLS-1$
				} else if(DominoConstants.FIELD_ADDED.equals(name)) {
					name = "@AddedToThisFile"; //$NON-NLS-1$
				} else if(DominoConstants.FIELD_MODIFIED_IN_THIS_FILE.equals(name)) {
					name = "@ModifiedInThisFile"; //$NON-NLS-1$
				}
				break;
		}

		Object value = document.get();
		if(value != null && value.getClass().isEnum()) {
			value = value.toString();
		}
		switch (condition.condition()) {
			case EQUALS: {
				if(value instanceof Number n) {
					return DQL.item(name).isEqualTo(n.doubleValue());
				} else if(value instanceof Temporal t) {
					return DQL.item(name).isEqualTo(t);
				} else {
					return DQL.item(name).isEqualTo(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case LESSER_THAN: {
				if(value instanceof Number n) {
					return DQL.item(name).isLessThan(n.doubleValue());
				} else if(value instanceof Temporal t) {
					return DQL.item(name).isLessThan(t);
				} else {
					return DQL.item(name).isLessThan(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case LESSER_EQUALS_THAN: {
				if(value instanceof Number n) {
					return DQL.item(name).isLessThanOrEqual(n.doubleValue());
				} else if(value instanceof Temporal t) {
					return DQL.item(name).isLessThanOrEqual(t);
				} else {
					return DQL.item(name).isLessThanOrEqual(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case GREATER_THAN: {
				if(value instanceof Number n) {
					return DQL.item(name).isGreaterThan(n.doubleValue());
				} else if(value instanceof Temporal t) {
					return DQL.item(name).isGreaterThan(t);
				} else {
					return DQL.item(name).isGreaterThan(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case GREATER_EQUALS_THAN: {
				if(value instanceof Number n) {
					return DQL.item(name).isGreaterThanOrEqual(n.doubleValue());
				} else if(value instanceof Temporal t) {
					return DQL.item(name).isGreaterThanOrEqual(t);
				} else {
					return DQL.item(name).isGreaterThanOrEqual(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case LIKE: {
				if(value instanceof Number) {
					throw new IllegalArgumentException("Unable to perform LIKE query on a number");
				} else {
					return DQL.item(name).contains(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			}
			case IN: {
				Object arr = toDqlArray(value);
				if(arr instanceof int[] i) {
					return DQL.item(name).in(i);
				} else if(arr instanceof double[] d) {
					return DQL.item(name).in(d);
				} else {
					// Guaranteed to be String[]
					return DQL.item(name).in((String[])arr);
				}
			}
			case AND: {
				List<CriteriaCondition> conditions = document.get(new TypeReference<List<CriteriaCondition>>() {});
				return DQL.and(conditions
					.stream()
					.map(QueryConverter::getCondition)
					.toArray(DQLTerm[]::new));
			}
			case OR: {
				List<CriteriaCondition> conditions = document.get(new TypeReference<List<CriteriaCondition>>() {});
				return DQL.or(conditions
					.stream()
					.map(QueryConverter::getCondition)
					.toArray(DQLTerm[]::new));
			}
			case NOT: {
				CriteriaCondition dc = document.get(CriteriaCondition.class);
				return DQL.not(getCondition(dc));
			}
			default:
				throw new IllegalStateException("This condition is not supported in Domino: " + condition.condition()); //$NON-NLS-1$
		}
	}

	public static class QueryConverterResult {

		private final String[] unids;
		private final DQLTerm dql;
		private final long skip;
		private final long limit;

		QueryConverterResult(final String[] unids, final DQLTerm dql, final long skip, final long limit) {
			this.unids = unids;
			this.dql = dql;
			this.skip = skip;
			this.limit = limit;
		}

		public String[] getUnids() {
			return unids;
		}

		public DQLTerm getStatement() {
			return dql;
		}

		public long getSkip() {
			return skip;
		}

		public long getLimit() {
			return limit;
		}
	}


	private static DQLTerm applyFormName(final DQLTerm condition, final String formName) {
		if(formName == null || formName.isEmpty()) {
			return condition;
		} else {
			if(condition == null) {
				return DQL.item(DominoConstants.FIELD_NAME).isEqualTo(formName);
			} else {
				return DQL.and(DQL.item(DominoConstants.FIELD_NAME).isEqualTo(formName), condition);
			}
		}
	}

	/**
	 * Converts the provided value to a DQL-usable array:
	 * either a String[], int[], or double[].
	 */
	@SuppressWarnings("unchecked")
	private static Object toDqlArray(final Object value) {
		if(value == null) {
			return EMPTY_STRING_ARRAY;
		} else if(value instanceof String s) {
			return new String[] { s };
		} else if(value instanceof String[] s) {
			return s;
		} else if(value instanceof Integer i) {
			return new int[] { i };
		} else if(value instanceof int[] s) {
			return s;
		} else if(value instanceof double[] s) {
			return s;
		} else if(value instanceof float[] s) {
			double[] result = new double[s.length];
			for(int i = 0; i < s.length; i++) {
				result[i] = s[i];
			}
			return result;
		} else if(value instanceof long[] s) {
			double[] result = new double[s.length];
			for(int i = 0; i < s.length; i++) {
				result[i] = s[i];
			}
			return result;
		} else if(value instanceof short[] s) {
			int[] result = new int[s.length];
			for(int i = 0; i < s.length; i++) {
				result[i] = s[i];
			}
			return result;
		} else if(value instanceof boolean[] b) {
			int[] result = new int[b.length];
			for(int i = 0; i < b.length; i++) {
				result[i] = b[i] ? 1 : 0;
			}
			return result;
		} else if(value instanceof Number n) {
			return new double[] { n.doubleValue() };
		} else if(value.getClass().isArray()) {
			Class<?> componentType = value.getClass().getComponentType();
			if(Number.class.isAssignableFrom(componentType)) {
				double[] result = new double[Array.getLength(value)];
				for(int i = 0; i < result.length; i++) {
					Number n = (Number)Array.get(value, i);
					result[i] = n == null ? 0 : n.doubleValue();
				}
				return result;
			} else {
				String[] result = new String[Array.getLength(value)];
				for(int i = 0; i < result.length; i++) {
					Object o = Array.get(value, i);
					result[i] = o == null ? "" : o.toString(); //$NON-NLS-1$
				}
				return result;
			}
		} else if(value instanceof Iterable i) {
			Iterator<?> iter = i.iterator();
			if(!iter.hasNext()) {
				return EMPTY_STRING_ARRAY;
			} else {
				Object o = iter.next();
				if(o instanceof Integer) {
					Spliterator<Object> s = i.spliterator();
					return StreamSupport.stream(s, false)
						.map(Number.class::cast)
						.mapToInt(Number::intValue)
						.toArray();
				} else if(o instanceof Number) {
					Spliterator<Object> s = i.spliterator();
					return StreamSupport.stream(s, false)
						.map(Number.class::cast)
						.mapToDouble(Number::doubleValue)
						.toArray();
				} else {
					Spliterator<Object> s = i.spliterator();
					return StreamSupport.stream(s, false)
						.map(o2 -> o2 == null ? "" : o2.toString()) //$NON-NLS-1$
						.toArray(String[]::new);
				}
			}
		} else {
			return new String[] { value.toString() };
		}
	}
}