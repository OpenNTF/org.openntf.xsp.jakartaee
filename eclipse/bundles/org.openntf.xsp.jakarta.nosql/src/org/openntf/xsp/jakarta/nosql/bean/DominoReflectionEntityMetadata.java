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
public class DominoReflectionEntityMetadata implements GroupEntityMetadata {

    private static final Logger LOGGER = Logger.getLogger(DominoReflectionEntityMetadata.class.getName());

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
        LOGGER.fine("Starting the scanning process for Entity and Embeddable annotations: ");
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
                .filter(l -> l.isLoggable(Level.FINEST))
                .ifPresent(l -> l.fine("Finishing the scanning with: %d Entity and Embeddable scanned classes and %s Named entities"
                        .formatted(entityMetadataByClass.size(), entityMetadataByEntityName.size())));

    }
}
