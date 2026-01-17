/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.bean;


import static java.util.Optional.ofNullable;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.interceptor.Interceptor;

/**
 * This variant of {@link org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension ReflectionEntityMetadataExtension}
 * avoids the use of static maps for entity metadata storage.
 * 
 * @since 3.4.0
 */
@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
public class DominoReflectionEntityMetadata implements GroupEntityMetadata {

    private static final Logger LOGGER = System.getLogger(DominoReflectionEntityMetadata.class.getName());

    private final Map<Class<?>, EntityMetadata> entityMetadataByClass = new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> entityMetadataByEntityName = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, EntityMetadata> mappings() {
        return entityMetadataByEntityName;
    }

    @Override
    public Map<Class<?>, EntityMetadata> classes() {
        return entityMetadataByClass;
    }

    @PostConstruct
    public void scanEntitiesAndEmbeddableEntities() {
        LOGGER.log(Level.TRACE, "Starting the scanning process for Entity and Embeddable annotations: ");
        ClassConverter converter = ClassConverter.load();
        ClassScanner scanner = ClassScanner.load();
        scanner.entities()
                .forEach(entity -> {
                    EntityMetadata entityMetadata = converter.apply(entity);
                    if (entityMetadata.hasEntityName()) {
                    	entityMetadataByEntityName.put(entityMetadata.name(), entityMetadata);
                    }
                    entityMetadataByClass.put(entity, entityMetadata);
                });

        scanner.embeddables()
                .forEach(embeddable -> {
                    EntityMetadata entityMetadata = converter.apply(embeddable);
                    entityMetadataByClass.put(embeddable, entityMetadata);
                });

        ofNullable(LOGGER)
                .filter(l -> l.isLoggable(Level.TRACE))
                .ifPresent(l -> l.log(Level.TRACE, "Finishing the scanning with: %d Entity and Embeddable scanned classes and %s Named entities"
                        .formatted(entityMetadataByClass.size(), entityMetadataByEntityName.size())));

    }
}
