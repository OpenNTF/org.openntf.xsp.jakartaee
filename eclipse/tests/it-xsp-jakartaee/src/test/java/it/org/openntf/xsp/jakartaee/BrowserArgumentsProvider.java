package it.org.openntf.xsp.jakartaee;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.testcontainers.containers.BrowserWebDriverContainer;

public class BrowserArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
		// Just HtmlUnit for now
		return Stream.of(
			JakartaTestContainers.instance.firefox
		)
		.map(BrowserWebDriverContainer::getWebDriver)
		.map(Arguments::of);
	}

}
