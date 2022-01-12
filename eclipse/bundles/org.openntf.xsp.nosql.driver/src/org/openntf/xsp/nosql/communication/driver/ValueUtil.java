/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.nosql.communication.driver;

import jakarta.nosql.Value;
import jakarta.nosql.ValueWriter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.communication.writer.ValueWriterDecorator;

import static java.util.stream.Collectors.toList;

/**
 * Utilitarian class to {@link Value}
 */
class ValueUtil {

    @SuppressWarnings("rawtypes")
	private static final ValueWriter VALUE_WRITER = ValueWriterDecorator.getInstance();
    @SuppressWarnings("rawtypes")
	private static final Function CONVERT = o -> {
        if (o instanceof Value) {
            return convert(Value.class.cast(o));
        }
        return getObject(o);
    };

    private ValueUtil() {
    }

    /**
     * converter a {@link Value} to Object
     *
     * @param value the value
     * @return a object converted
     */
    public static Object convert(Value value) {
        Objects.requireNonNull(value, "value is required");
        Object val = value.get();
        if(val instanceof Iterable) {
            return getObjects(val);
        }
        return getObject(val);
    }


    /**
     * Converts the {@link Value} to {@link List}
     *
     * @param value the value
     * @return a list object
     */
    public static List<Object> convertToList(Value value) {
        Objects.requireNonNull(value, "value is required");
        Object val = value.get();
        if(val instanceof Iterable) {
            return getObjects(val);

        }
        return Collections.singletonList(getObject(val));
    }

    @SuppressWarnings("unchecked")
	private static List<Object> getObjects(Object val) {
        return (List<Object>) StreamSupport.stream(Iterable.class.cast(val).spliterator(), false)
                .map(CONVERT).collect(toList());
    }

    @SuppressWarnings("unchecked")
	private static Object getObject(Object val) {
        if (VALUE_WRITER.test(val.getClass())) {
            return VALUE_WRITER.write(val);
        }
        return val;
    }
}