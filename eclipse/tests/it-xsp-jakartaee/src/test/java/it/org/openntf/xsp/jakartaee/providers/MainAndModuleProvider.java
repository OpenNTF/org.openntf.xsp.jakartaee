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
package it.org.openntf.xsp.jakartaee.providers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;

/**
 * Provides arguments for both the NSFComponentModule and NSFJakartaModule variants
 * of the primary test DB.
 */
public enum MainAndModuleProvider {
	;
	
	public static final Collection<TestDatabase> VALS = List.of(TestDatabase.MAIN, TestDatabase.MAIN_MODULE);
	
	public static class EnumOnly implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return VALS.stream().map(Arguments::of);
		}
	}
	
	public static class EnumAndBrowser implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			var browsers = new BrowserArgumentsProvider();
			return new EnumOnly().provideArguments(context)
				.map(arg -> arg.get()[0])
				.flatMap(e ->
					browsers.provideArguments(context)
						.map(arg -> arg.get()[0])
						.map(browser -> Arguments.of(e, browser))
				);
		}
	}
	
	public static class EnumAndAnonymousClient implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			var client = new AbstractWebClientTest.AnonymousClientProvider();
			return new EnumOnly().provideArguments(context)
				.map(arg -> arg.get()[0])
				.flatMap(e ->
					client.provideArguments(context)
						.map(arg -> arg.get()[0])
						.map(browser -> Arguments.of(e, browser))
				);
		}
	}
	
	public static class EnumAndBoolean implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return new MainAndModuleProvider.EnumOnly().provideArguments(context)
				.map(args -> args.get()[0])
				.flatMap(e -> {
					return Stream.of(
						Arguments.of(e, true),
						Arguments.of(e, false)
					);
				});
		}
	}
	
}
