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
package bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;

@ApplicationScoped
public class JsonbConfigProvider {
	public enum FieldVisibility implements PropertyVisibilityStrategy {
		INSTANCE;

		@Override
		public boolean isVisible(Field field) {
			return true;
		}

		@Override
		public boolean isVisible(Method method) {
			return false;
		}
		
	}
	
	@Produces
	public JsonbConfig getConfig() {
		return new JsonbConfig()
			.withPropertyVisibilityStrategy(FieldVisibility.INSTANCE);
	}
}
