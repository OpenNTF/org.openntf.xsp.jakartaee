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
package org.openntf.xsp.jakarta.nosql.communication.driver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;

/**
 * This class allows for the creation of in-memory attachments in a way.
 *
 * <p>This is similar to the class used by {@link EntityAttachment#of}, but
 * is {@code public} to avoid trouble with JSON serialization via Yasson.</p>
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class ByteArrayEntityAttachment implements EntityAttachment {
    private final String name;
    private final String contentType;
    private final long lastModified;
    private final byte[] data;

    public ByteArrayEntityAttachment(final String name, final String contentType, final long lastModified, final byte[] data) {
        this.name = name;
        this.contentType = contentType;
        this.lastModified = lastModified;
        this.data = data;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getData() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public long getLength() {
        return data.length;
    }

}
