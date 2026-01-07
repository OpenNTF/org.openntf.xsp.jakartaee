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
package org.openntf.xsp.jakartaee.jasapi;

/**
 * Extension class that can be registered inside an NSF in
 * {@code META-INF/services} to handle JavaSapi events for requests in the
 * context of the NSF.
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public interface JavaSapiExtension {
	/**
	 * Result codes for JavaSapi operations
	 */
	enum Result {
		SUCCESS(0),
		REQUEST_PROCESSED(1),
		EVENT_HANDLED(2),
		EVENT_DECLINED(3),
		REQUEST_AUTHENTICATED(4),
		TRANSLATED_DONE(5),
		TRANSLATED_CONTINUE(6);

		private final int status;

		private Result(final int status) {
			this.status = status;
		}

		public int getStatus() {
			return status;
		}
	}

	default Result authenticate(final JavaSapiContext context) {
		return Result.EVENT_DECLINED;
	}

	default void endRequest(final JavaSapiContext context) {
	}

	default Result processRequest(final JavaSapiContext context) {
		return Result.EVENT_DECLINED;
	}

	default Result rawRequest(final JavaSapiContext context) {
		return Result.EVENT_DECLINED;
	}

	default Result rewriteURL(final JavaSapiContext context) {
		return Result.EVENT_DECLINED;
	}
}
