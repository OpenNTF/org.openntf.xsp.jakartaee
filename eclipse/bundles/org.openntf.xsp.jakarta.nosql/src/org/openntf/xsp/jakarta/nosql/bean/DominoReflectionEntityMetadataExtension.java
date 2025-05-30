package org.openntf.xsp.jakarta.nosql.bean;


import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class DominoReflectionEntityMetadataExtension implements GroupEntityMetadata {

    private static final Logger LOGGER = Logger.getLogger(DominoReflectionEntityMetadataExtension.class.getName());

    private final Map<Class<?>, EntityMetadata> ENTITY_METADATA_BY_CLASS = new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> ENTITY_METADATA_BY_ENTITY_NAME = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, EntityMetadata> mappings() {
        return ENTITY_METADATA_BY_ENTITY_NAME;
    }

    @Override
    public Map<Class<?>, EntityMetadata> classes() {
        return ENTITY_METADATA_BY_CLASS;
    }

    @PostConstruct
    public void scanEntitiesAndEmbeddableEntities() {
        LOGGER.fine("Starting the scanning process for Entity and Embeddable annotations: ");
        ClassConverter converter = ClassConverter.load();
        ClassScanner scanner = ClassScanner.load();
        scanner.entities()
                .forEach(entity -> {
                    EntityMetadata entityMetadata = converter.apply(entity);
                    if (entityMetadata.hasEntityName()) {
                        ENTITY_METADATA_BY_ENTITY_NAME.put(entityMetadata.name(), entityMetadata);
                    }
                    ENTITY_METADATA_BY_CLASS.put(entity, entityMetadata);
                });

        scanner.embeddables()
                .forEach(embeddable -> {
                    EntityMetadata entityMetadata = converter.apply(embeddable);
                    ENTITY_METADATA_BY_CLASS.put(embeddable, entityMetadata);
                });

        ofNullable(LOGGER)
                .filter(l -> l.isLoggable(Level.FINEST))
                .ifPresent(l -> l.fine("Finishing the scanning with: %d Entity and Embeddable scanned classes and %s Named entities"
                        .formatted(ENTITY_METADATA_BY_CLASS.size(), ENTITY_METADATA_BY_ENTITY_NAME.size())));

    }
}
