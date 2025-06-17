package org.openntf.xsp.jakartaee.module.jakarta;

import java.nio.ByteBuffer;

/**
 * Represents an icon for a module.
 * 
 * @since 3.5.0
 */
public record ModuleIcon(String mimeType, int width, int height, ByteBuffer data) {

}
