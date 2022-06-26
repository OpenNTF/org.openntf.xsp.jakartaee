package org.openntf.xsp.nosql.communication.driver;

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
    
    public ByteArrayEntityAttachment(String name, String contentType, long lastModified, byte[] data) {
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
