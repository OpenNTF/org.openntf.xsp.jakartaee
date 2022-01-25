/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.nosql.communication.driver;

import static jakarta.nosql.Condition.IN;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.openntf.xsp.nosql.communication.driver.DQL.DQLTerm;

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

	static QueryConverterResult select(DocumentQuery query) {
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
		String name = document.getName();
		if (String.valueOf(name).equals(EntityConverter.ID_FIELD)) {
			name = "@DocumentUniqueID"; //$NON-NLS-1$
		}

		Object placeholder = document.get();
		if(placeholder != null && placeholder.getClass().isEnum()) {
			placeholder = placeholder.toString();
		}
		switch (condition.getCondition()) {
			case EQUALS:
				if(placeholder instanceof Number) {
					return DQL.item(name).isEqualTo(((Number)placeholder).doubleValue());
				} else {
					return DQL.item(name).isEqualTo(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case LESSER_THAN:
				if(placeholder instanceof Number) {
					return DQL.item(name).isLessThan(((Number)placeholder).doubleValue());
				} else {
					return DQL.item(name).isLessThan(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case LESSER_EQUALS_THAN:
				if(placeholder instanceof Number) {
					return DQL.item(name).isLessThanOrEqual(((Number)placeholder).doubleValue());
				} else {
					return DQL.item(name).isLessThanOrEqual(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case GREATER_THAN:
				if(placeholder instanceof Number) {
					return DQL.item(name).isGreaterThan(((Number)placeholder).doubleValue());
				} else {
					return DQL.item(name).isGreaterThan(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case GREATER_EQUALS_THAN:
				if(placeholder instanceof Number) {
					return DQL.item(name).isGreaterThanOrEqual(((Number)placeholder).doubleValue());
				} else {
					return DQL.item(name).isGreaterThanOrEqual(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case LIKE:
				// TODO investigate @Like
				if(placeholder instanceof Number) {
					throw new IllegalArgumentException("Unable to perform LIKE query on a number");
				} else {
					return DQL.item(name).contains(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
				}
			case IN:
				if(placeholder instanceof Number) {
					throw new IllegalArgumentException("Unable to perform IN query on a number");
				} else {
					return DQL.item(name).contains(placeholder == null ? "" : placeholder.toString()); //$NON-NLS-1$
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

	static class QueryConverterResult {

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

		DQLTerm getStatement() {
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
				return DQL.item(EntityConverter.NAME_FIELD).isEqualTo(formName);
			} else {
				return DQL.and(condition, DQL.item(EntityConverter.NAME_FIELD).isEqualTo(formName));
			}
		}
	}
}