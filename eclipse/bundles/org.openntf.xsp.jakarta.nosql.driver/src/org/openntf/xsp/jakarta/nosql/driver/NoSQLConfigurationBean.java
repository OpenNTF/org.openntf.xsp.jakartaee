package org.openntf.xsp.jakarta.nosql.driver;

/**
 * This interface can be used to define a bean within
 * an application to handle configuring driver behavior.
 * 
 * @since 3.5.0
 */
public interface NoSQLConfigurationBean {
	/**
	 * Determines whether the driver should emit a CDI
	 * event containing "explain" results when performing
	 * a DQL operation.
	 * 
	 * @return {@code true} if the driver should emit
	 *         explain results during operations
	 */
	boolean emitExplainEvents();
}
