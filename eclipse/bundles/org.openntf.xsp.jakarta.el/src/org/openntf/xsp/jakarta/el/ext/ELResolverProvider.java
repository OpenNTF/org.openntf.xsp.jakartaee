/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.el.ext;

import java.util.Collection;

import jakarta.el.ELResolver;

/**
 * Service extension point to allow client code to provide custom {@link ELResolver}
 * implementations to be added to the XPages replacement resolver.
 *
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@FunctionalInterface
public interface ELResolverProvider {
	Collection<ELResolver> provide();
}
