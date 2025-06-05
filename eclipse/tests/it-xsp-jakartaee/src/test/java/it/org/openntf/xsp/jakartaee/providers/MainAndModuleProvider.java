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
	
}
