/*
 *  Copyright (c) 2017-2019 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Jesse Gallagher
 */
package org.eclipse.jnosql.communication.driver.attachment;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Default representation of {@link EntityAttachment} for a filesystem {@link Path}.
 * 
 * @since 0.0.9
 */
class PathEntityAttachment implements EntityAttachment {
    private final Path path;
    
    public PathEntityAttachment(Path path) {
        Objects.requireNonNull(path, "path cannot be null");
        if(!Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("Path is not a readable file: " + path);
        }
        
        this.path = path;
    }

    @Override
    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    public long getLastModified() {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getContentType() {
        try {
            return Files.probeContentType(path);
        } catch(IOException e) {
            throw new UncheckedIOException("There is an error to load the content type", e);
        }
    }

    @Override
    public InputStream getData() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public long getLength() {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}