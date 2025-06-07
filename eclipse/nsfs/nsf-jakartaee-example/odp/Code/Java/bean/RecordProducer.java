/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package bean;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import model.RecordExample;

/**
 * This bean is used by tests to ensure that reading record properties in
 * XPages, JSP, and Faces contexts works.
 */
@RequestScoped
@Named("RecordProducer")
public class RecordProducer {
	public RecordExample getRecordExample() {
		return new RecordExample("1111", "I am the example", 3);
	}
	
	public Optional<RecordExample> getOptionalEmpty() {
		return Optional.empty();
	}
	
	public Optional<RecordExample> getOptionalFull() {
		return Optional.of(new RecordExample("2222", "I am the optional example", 4));
	}
}
