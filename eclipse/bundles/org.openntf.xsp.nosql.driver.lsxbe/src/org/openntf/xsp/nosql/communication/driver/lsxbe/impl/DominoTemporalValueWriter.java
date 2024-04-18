/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
