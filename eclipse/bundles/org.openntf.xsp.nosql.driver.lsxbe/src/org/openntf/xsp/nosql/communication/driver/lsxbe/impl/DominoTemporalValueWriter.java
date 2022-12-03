package org.openntf.xsp.nosql.communication.driver.lsxbe.impl;

import java.time.temporal.TemporalAccessor;
import jakarta.annotation.Priority;
import jakarta.nosql.ValueWriter;

/**
 * This no-op {@link ValueWriter} implementation handles the case of {@link TemporalAccessor}s
 * to be written to Domino. The driver handles conversion to DateTime specially, and this is
 * intended to override the default JNoSQL implementation, which converts to a String.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(1)
public class DominoTemporalValueWriter implements ValueWriter<TemporalAccessor, TemporalAccessor> {

	@Override
	public boolean test(Class<?> type) {
        return TemporalAccessor.class.isAssignableFrom(type);
	}

	@Override
	public TemporalAccessor write(TemporalAccessor object) {
		return object;
	}

}
