# XPages Jakarta EE Support

This project adds partial support for several Java/Jakarta EE technologies to XPages applications. Of the [list of technologies](http://www.oracle.com/technetwork/java/javaee/tech/index.html) included in the full JEE 8 spec, this project currently provides:

- Expression Language 3.0
- Context and Dependency Injection for Java 2.0
  - Common Annotations for the Java Platform 1.3
  - Interceptors 1.2
  - Dependency Injection for Java 1.0
- Java API for RESTful Web Services (JAX-RS) 2.1

## CDI 2.0

The [Context and Dependency Injection for Java EE (CDI) 2.0](http://cdi-spec.org/) specification provides for managed beans and dependency injection. To use this feature, add the "org.openntf.xsp.cdi" library to your XPages app.

Currently, this support is focused around adding annotated CDI managed bean classes in an NSF and having them picked up by the variable resolver. For example:

```java
@ApplicationScoped
@Named("applicationGuy")
public class ApplicationGuy {
  public void getFoo() {
    return "hello";
  }
}
```

```xml
<xp:text value="#{applicationGuy.foo}"/>
```

These beans are managed and instantiated by [Weld](http://weld.cdi-spec.org) and support injection with other annotated beans:

```java
@RequestScoped
@Named("requestGuy")
public class RequestGuy {
	@Inject private ApplicationGuy applicationGuy;
	// Or with a name:
	@Inject @Named("applicationGuy") private ApplicationGuy applicationGuy;
  
	// ...
}
```

Additionally, the CDI system can inject implementations of interfaces based on available concrete implementations, as well as use `@PostConstruct` and `@PreDestroy` annotations:

```java
public class RequestGuy extends AbstractBean {
	static interface AppModel {

	}
	static class SomeModelClass implements AppModel {
		
	}
	@Inject AppModel model; // Will be a new instance of SomeModelClass each request
	
	@PostConstruct
	public void postConstruct() { System.out.println("Created with " + model); }
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying!"); }
}
```

### Conversation Scope

This implementation maps CDI `@ConversationScoped` beans to the XPages view scope. This isn't necessarily a [direct analogue](https://stackoverflow.com/questions/7788430/how-does-jsf-2-conversationscope-work), but it's close enough.

### Limitations

Currently, the CDI environment for the application acts as if there is a `META-INF/beans.xml` file with `bean-discovery-mode="all"` set, but only resolves within the active NSF. So, while NSF beans and classes can reference each other, plugin and system classes are not available for CDI injection.

## Expression Language 3.0

The [Expression Language 3.0](https://jcp.org/en/jsr/detail?id=341) spec is the evolved version of the original Expression Language as used in XPages. It contains numerous improvements over its predecessors, such as method parameters and [lambda expressions](http://www.baeldung.com/jsf-expression-language-el-3). To use this feature, add the "org.openntf.xsp.el3" library to your XPages app.

When the library is enabled, the EL 3 processor takes over for all normal expression language bindings and so can be used without a prefix in some cases:

```xml
<xp:text value="${someBean.calculateFoo('some arg')}"/>
```

Note that Designer attempts to validate the syntax of runtime EL bindings; to work around this, add an "el:" prefix to the binding. This will leave a warning in Designer, but it will work:

```xml
<xp:text value="#{el:someBean.hello()}"/>
```

### Implementation Details

The EL 3 handler is currently stricter about null values than the default handler in XPages. For example, take this binding:

```xml
<xp:text value="${beanThatDoesNotExist.someProp}"/>
```

In standard XPages, this will result in an empty output. With the EL 3 resolver, however, this will cause an exception like `ELResolver cannot handle a null base Object with identifier 'beanThatDoesNotExist'`. I'm considering changing this behavior to match the XPages default, but there's also some value in the strictness, especially because the exception is helpful in referencing the object it's trying to resolve against, which could help track down subtle bugs.

## JAX-RS 2.1

The [JAX-RS](https://jcp.org/en/jsr/detail?id=370) specification is the standard way to provide web services in Java EE applications. A version of it has been included for a long time in Domino by way of the Extension Library. However, this version is also out of date, with Apache Wink implementing JAX-RS 1.1.1.

This library is based on [the work of Martin Pradny](https://www.pradny.com/2017/11/using-jax-rs-inside-nsf.html) and provides JAX-RS 2.1 support by way of [RESTEasy 3.5.1](https://resteasy.github.io) for classes inside the NSF. When a class is or has a method annotated with `@Path`, it is included as a service beneath `/xsp/.jaxrs` inside the NSF. For example:

```java
package servlet;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import beans.ApplicationGuy;

@Path("/sample")
public class Sample {
  @Inject private ApplicationGuy applicationGuy;
  
	@GET
	public Response hello() {
		try {
			return Response.ok()
				.type(MediaType.TEXT_PLAIN)
				.entity(applicationGuy.toString())
				.build();
		} catch(Throwable t) {
			return Response.serverError().build();
		}
	}
}
```

As intimated there, it has access to the CDI environment if enabled, though it doesn't yet have proper lifecycle support for `ConversationScoped` beans.

## Requirements

- Domino FP8+
- Designer FP10+ (for compiling the NSF)

## Building

To build this application, first `package` the `osgi-deps` Maven project, which will provide the target platform dependencies used by the `eclipse` Maven tree.

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under compatible licenses - see NOTICE for details.