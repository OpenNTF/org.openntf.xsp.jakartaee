/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import static org.eclipse.jnosql.communication.Condition.IN;

import java.time.temporal.Temporal;
import java.util.EnumSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.lang.Iterable;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
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

	private static final Set<Condition> NOT_APPENDABLE = EnumSet.of(IN, Condition.AND, Condition.OR);

	private static final String[] ALL_SELECT = { "*" }; //$NON-NLS-1$

	public static QueryConverterResult select(SelectQuery query) {
		String[] documents = query.columns().toArray(new String[0]);
		if (documents.length == 0) {
			documents = ALL_SELECT;
		}

		DQLTerm statement;
		long skip = query.skip();
		long limit = query.limit();

		if (query.condition().isPresent()) {
			statement = getCondition(query.condition().get());
			// Add in the form property if needed
			statement = applyFormName(statement, query.name());
		} else {
			statement = applyFormName(null, query.name());
		}
		return new QueryConverterResult(documents, statement, skip, limit);
	}

	public static DQLTerm getCondition(CriteriaCondition condition) {
		Element document = condition.element();

		if (!NOT_APPENDABLE.contains(condition.condition())) {
			// TODO determine if this is relevant
//			params = EntityConverter.add(params, document.getName(), document.get());
		}

		// Convert special names
		String name = String.valueOf(document.name());
		if (DominoConstants.FIELD_ID.equals(name)) {
			name = "@DocumentUniqueID"; //$NON-NLS-1$
		} else if(DominoConstants.FIELD_CDATE.equals(name)) {
			name = "@Created"; //$NON-NLS-1$
		} else if(DominoConstants.FIELD_MDATE.equals(name)) {
			name = "@Modified"; //$NON-NLS-1$
		} else if("$REF".equalsIgnoreCase(name)) { //$NON-NLS-1$
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

		Object value = document.get();
		if(value != null && value.getClass().isEnum()) {
			value = value.toString();
		}
		switch (condition.condition()) {
			case EQUALS:
				if(value instanceof Number) {
					return DQL.item(name).isEqualTo(((Number)value).doubleValue());
				} else if(value instanceof Temporal) {
					return DQL.item(name).isEqualTo((Temporal)value);
				} else {
					return DQL.item(name).isEqualTo(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case LESSER_THAN:
				if(value instanceof Number) {
					return DQL.item(name).isLessThan(((Number)value).doubleValue());
				} else if(value instanceof Temporal) {
					return DQL.item(name).isLessThan((Temporal)value);
				} else {
					return DQL.item(name).isLessThan(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case LESSER_EQUALS_THAN:
				if(value instanceof Number) {
					return DQL.item(name).isLessThanOrEqual(((Number)value).doubleValue());
				} else if(value instanceof Temporal) {
					return DQL.item(name).isLessThanOrEqual((Temporal)value);
				} else {
					return DQL.item(name).isLessThanOrEqual(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case GREATER_THAN:
				if(value instanceof Number) {
					return DQL.item(name).isGreaterThan(((Number)value).doubleValue());
				} else if(value instanceof Temporal) {
					return DQL.item(name).isGreaterThan((Temporal)value);
				} else {
					return DQL.item(name).isGreaterThan(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case GREATER_EQUALS_THAN:
				if(value instanceof Number) {
					return DQL.item(name).isGreaterThanOrEqual(((Number)value).doubleValue());
				} else if(value instanceof Temporal) {
					return DQL.item(name).isGreaterThanOrEqual((Temporal)value);
				} else {
					return DQL.item(name).isGreaterThanOrEqual(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case LIKE:
				// TODO investigate @Like
				if(value instanceof Number) {
					throw new IllegalArgumentException("Unable to perform LIKE query on a number");
				} else {
					return DQL.item(name).contains(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case IN:
				if(value instanceof Number) {
					throw new IllegalArgumentException("Unable to perform IN query on a number");
                } else {
                    List<String> valueHelper = new ArrayList<>();

                    // Make sure value is valid.
                    if (value == null) {
                        value = "";
                    } else if (value.getClass().isArray()) {
                        value = Arrays.asList(value);
                    }
                    
                    // If value is an Iterable, convert all entries to String.
                    if (value instanceof Iterable) {
                        ((Iterable<?>)value).forEach(item -> valueHelper.add(item.toString()));
                    } else {
                        valueHelper.add(value.toString());
                    }
					return DQL.item(name).contains(valueHelper.toArray(new String[valueHelper.size()])); //$NON-NLS-1$
				}
			case AND: {
				List<CriteriaCondition> conditions = document.get(new TypeReference<List<CriteriaCondition>>() {});
				return DQL.and(conditions
					.stream()
					.map(c -> getCondition(c))
					.toArray(DQLTerm[]::new));
			}
			case OR: {
				List<CriteriaCondition> conditions = document.get(new TypeReference<List<CriteriaCondition>>() {});
				return DQL.or(conditions
					.stream()
					.map(c -> getCondition(c))
					.toArray(DQLTerm[]::new));
			}
			case NOT:
				CriteriaCondition dc = document.get(CriteriaCondition.class);
				return DQL.not(getCondition(dc));
			default:
				throw new IllegalStateException("This condition is not supported in Domino: " + condition.condition()); //$NON-NLS-1$
		}
	}

	public static class QueryConverterResult {

		private final String[] unids;
		private final DQLTerm dql;
		private final long skip;
		private final long limit;

		QueryConverterResult(String[] unids, DQLTerm dql, long skip, long limit) {
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

	
	private static DQLTerm applyFormName(DQLTerm condition, String formName) {
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
}