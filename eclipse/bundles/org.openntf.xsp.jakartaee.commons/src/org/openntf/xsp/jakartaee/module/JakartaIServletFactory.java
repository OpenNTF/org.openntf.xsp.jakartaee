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
package org.openntf.xsp.jakartaee.module;

import com.ibm.designer.runtime.domino.adapter.IServletFactory;

/**
 * This subinterface of {@link IServletFactory} designates an implication
 * that is "Jakarta-aware": that is, it expects to handle modules that
 * are not always {@code NSFComponentModule}.
 * 
 * @since 3.4.0
 */
public interface JakartaIServletFactory extends IServletFactory {

}
