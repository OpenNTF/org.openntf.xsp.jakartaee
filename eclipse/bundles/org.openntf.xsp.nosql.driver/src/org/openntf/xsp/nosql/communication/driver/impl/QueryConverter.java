/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.communication.driver.impl;

import static jakarta.nosql.Condition.IN;

import java.time.temporal.Temporal;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.communication.driver.impl.DQL.DQLTerm;

import jakarta.nosql.Condition;
import jakarta.nosql.TypeReference;
import jakarta.nosql.document.Document;
import jakarta.nosql.document.DocumentCondition;
import jakarta.nosql.document.DocumentQuery;

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

	public static QueryConverterResult select(DocumentQuery query) {
		String[] documents = query.getDocuments().toArray(new String[0]);
		if (documents.length == 0) {
			documents = ALL_SELECT;
		}

		DQLTerm statement;
		long skip = query.getSkip();
		long limit = query.getLimit();

		if (query.getCondition().isPresent()) {
			statement = getCondition(query.getCondition().get());
			// Add in the form property if needed
			statement = applyFormName(statement, query.getDocumentCollection());
		} else {
			statement = applyFormName(null, query.getDocumentCollection());
		}
		return new QueryConverterResult(documents, statement, skip, limit);
	}

	public static DQLTerm getCondition(DocumentCondition condition) {
		Document document = condition.getDocument();

		if (!NOT_APPENDABLE.contains(condition.getCondition())) {
			// TODO determine if this is relevant
//			params = EntityConverter.add(params, document.getName(), document.get());
		}

		// Convert special names
		String name = String.valueOf(document.getName());
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
		switch (condition.getCondition()) {
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
					return DQL.item(name).contains(value == null ? "" : value.toString()); //$NON-NLS-1$
				}
			case AND: {
				List<DocumentCondition> conditions = document.get(new TypeReference<List<DocumentCondition>>() {});
				return DQL.and(conditions
					.stream()
					.map(c -> getCondition(c))
					.toArray(DQLTerm[]::new));
			}
			case OR: {
				List<DocumentCondition> conditions = document.get(new TypeReference<List<DocumentCondition>>() {});
				return DQL.or(conditions
					.stream()
					.map(c -> getCondition(c))
					.toArray(DQLTerm[]::new));
			}
			case NOT:
				DocumentCondition dc = document.get(DocumentCondition.class);
				return DQL.not(getCondition(dc));
			default:
				throw new IllegalStateException("This condition is not supported in Darwino: " + condition.getCondition()); //$NON-NLS-1$
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
				return DQL.and(condition, DQL.item(DominoConstants.FIELD_NAME).isEqualTo(formName));
			}
		}
	}
}