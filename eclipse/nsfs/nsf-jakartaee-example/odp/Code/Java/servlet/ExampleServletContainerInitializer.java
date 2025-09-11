package servlet;

import java.text.MessageFormat;
import java.util.Set;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;

@HandlesTypes(ExampleServlet.class)
public class ExampleServletContainerInitializer implements ServletContainerInitializer {

	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		ctx.setAttribute(ExampleServletContainerInitializer.class.getName(), MessageFormat.format("I found {0} class", c.size()));
	}

}
