package org.openntf.xsp.nosql.communication.driver;

import java.util.function.Supplier;
import lotus.domino.Session;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@FunctionalInterface
public interface SessionSupplier extends Supplier<Session> {

}
