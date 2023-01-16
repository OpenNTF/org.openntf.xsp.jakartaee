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
package org.openntf.xsp.nosql.mapping.extension.impl;

import java.util.Objects;

import org.eclipse.jnosql.mapping.reflection.ClassMappings;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplate;
import org.openntf.xsp.nosql.mapping.extension.DominoTemplateProducer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import jakarta.nosql.mapping.Converters;
import jakarta.nosql.mapping.document.DocumentEntityConverter;
import jakarta.nosql.mapping.document.DocumentEventPersistManager;
import jakarta.nosql.mapping.document.DocumentWorkflow;

/**
 * Default implementation bean for {@link DominoTemplateProducer}.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
@ApplicationScoped
public class DefaultDominoTemplateProducer implements DominoTemplateProducer {
	@Inject
    private DocumentEntityConverter converter;

    @Inject
    private DocumentWorkflow workflow;

    @Inject
    private DocumentEventPersistManager persistManager;

    @Inject
    private ClassMappings mappings;

    @Inject
    private Converters converters;

	@Override
	public DominoTemplate get(DominoDocumentCollectionManager collectionManager) {
		Objects.requireNonNull(collectionManager, "collectionManager is required");
		
        return new ProducerDocumentTemplate(converter, collectionManager, workflow,
                persistManager, mappings, converters);
	}

    @Vetoed
    static class ProducerDocumentTemplate extends DefaultDominoTemplate {

        private DocumentEntityConverter converter;

        private DominoDocumentCollectionManager manager;

        private DocumentWorkflow workflow;

        private DocumentEventPersistManager persistManager;

        private Converters converters;

        private ClassMappings mappings;
        ProducerDocumentTemplate(DocumentEntityConverter converter, DominoDocumentCollectionManager manager,
                                 DocumentWorkflow workflow,
                                 DocumentEventPersistManager persistManager,
                                 ClassMappings mappings, Converters converters) {
            this.converter = converter;
            this.manager = manager;
            this.workflow = workflow;
            this.persistManager = persistManager;
            this.mappings = mappings;
            this.converters = converters;
        }

        ProducerDocumentTemplate() {
        }

        @Override
        protected DocumentEntityConverter getConverter() {
            return converter;
        }

        @Override
        protected DominoDocumentCollectionManager getManager() {
            return manager;
        }

        @Override
        protected DocumentWorkflow getWorkflow() {
            return workflow;
        }

        @Override
        protected DocumentEventPersistManager getPersistManager() {
            return persistManager;
        }

		@Override
		protected ClassMappings getClassMappings() {
			return mappings;
		}

        @Override
        protected Converters getConverters() {
            return converters;
        }
    }
}
