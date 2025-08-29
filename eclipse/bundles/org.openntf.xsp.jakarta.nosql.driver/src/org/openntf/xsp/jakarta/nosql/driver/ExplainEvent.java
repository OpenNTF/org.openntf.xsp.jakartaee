package org.openntf.xsp.jakarta.nosql.driver;

/**
 * This CDI event is fired when {@link NoSQLConfigurationBean#emitExplainEvents()}
 * is true for the application and a DQL query is going to be run.
 * 
 * @since 3.5.0
 */
public record ExplainEvent(String query, String server, String filePath, String explain) {

}
