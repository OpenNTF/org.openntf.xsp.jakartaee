# XPages Jakarta EE Support

This project adds partial support for several Java/Jakarta EE technologies to XPages applications. Of the [list of technologies](https://jakarta.ee/specifications/) included in the full Jakarta EE 9 spec, this project currently provides:

- Expression Language 4.0
- Contexts and Dependency Injection 3.0
  - Annotations 2.0
  - Interceptors 2.0
  - Dependency Injection 2.0
- RESTful Web Services (JAX-RS) 3.0
- Bean Validation 3.0
- JSON Processing 2.0
- JSON Binding 2.0
- XML Binding 3.0
- Mail 2.1
  - Activation 2.1

## CDI 3.0

The [Jakarta Contexts and Dependency Injection 3.0](https://jakarta.ee/specifications/cdi/3.0/) specification provides for managed beans and dependency injection. To use this feature, add the "org.openntf.xsp.cdi" library to your XPages app.

Currently, this support is focused around adding annotated CDI managed bean classes in an NSF and having them picked up by the variable resolver. For example:

```java
@ApplicationScoped
@Named("applicationGuy")
public class ApplicationGuy {
  public String getFoo() {
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

## Expression Language 4.0

The [Expression Language 4.0](https://jakarta.ee/specifications/expression-language/4.0/) spec is the evolved version of the original Expression Language as used in XPages. It contains numerous improvements over its predecessors, such as method parameters and [lambda expressions](http://www.baeldung.com/jsf-expression-language-el-3). To use this feature, add the "org.openntf.xsp.el" library to your XPages app.

When the library is enabled, the EL 4 processor takes over for all normal expression language bindings and so can be used without a prefix in some cases:

```xml
<xp:text value="${someBean.calculateFoo('some arg')}"/>
```

Note that Designer attempts to validate the syntax of runtime EL bindings; to work around this, add an "el:" prefix to the binding. This will leave a warning in Designer, but it will work:

```xml
<xp:text value="#{el:someBean.hello()}"/>
```

### Overriding the Prefix

If you don't want EL 4 to take over the default handling of EL in the app, you can specify a different prefix using the `org.openntf.xsp.el.prefix` in your app's Xsp Properties file. For example:

```properties
xsp.library.depends=org.openntf.xsp.el
org.openntf.xsp.el.prefix=ex
```

Note that Designer refuses to compile XPages with runtime-bound expressions that use a number in the prefix, so this should be letters only.

Regardless of whether or not you specify an alternate prefix, the original XPages EL parser will be available by using expressions like `#{xspel:someBean}`.

### Implementation Details

The EL 4 handler is currently stricter about null values than the default handler in XPages. For example, take this binding:

```xml
<xp:text value="${beanThatDoesNotExist.someProp}"/>
```

In standard XPages, this will result in an empty output. With the EL 4 resolver, however, this will cause an exception like `ELResolver cannot handle a null base Object with identifier 'beanThatDoesNotExist'`. I'm considering changing this behavior to match the XPages default, but there's also some value in the strictness, especially because the exception is helpful in referencing the object it's trying to resolve against, which could help track down subtle bugs.

## RESTful Web Services

The [RESTful Web Services](https://jakarta.ee/specifications/restful-ws/3.0/) specification is the standard way to provide web services in Java EE applications. A version of it has been included for a long time in Domino by way of the Extension Library. However, this version is also out of date, with Apache Wink implementing JAX-RS 1.1.1.

This library is based on [the work of Martin Pradny](https://www.pradny.com/2017/11/using-jax-rs-inside-nsf.html) and provides JAX-RS 3.0 support by way of [RESTEasy 6.0](https://resteasy.github.io) for classes inside the NSF. When a class is or has a method annotated with `@Path`, it is included as a service beneath `/xsp/.jaxrs` inside the NSF. For example:

```java
package servlet;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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

## Bean Validation 3.0

The [Bean Validation](https://jakarta.ee/specifications/bean-validation/3.0/) spec provides a standard mechanism for performing validation of arbitrary objects via annotations. XPages doesn't provide any type of bean validation mechanism - the closest things it provides are UI component validators, but those don't connect to the back-end objects at all.

This library provides validation annotations and a processor via [Hibernate Validator 7.0.1.Final](http://hibernate.org/validator/). Since there is no existing structure to hook into in the XPages runtime, bean validation must be called explicitly by your code, such as in a common "save" method in model objects. This is done by constructing a `Validator` object using the builder and then running it against a given bean. Due to the intricacies of the XPages runtime, code performing validation should be run from an OSGi plugin.

For generic use, this library provides an `org.openntf.xsp.beanvalidation.XPagesValidationUtil` class with methods to construct a `Validator` object and to use that validator to validate a bean:

```java
import org.openntf.xsp.beanvalidation.XPagesValidationUtil;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotEmpty;

public class Tester {
  public static class ExampleBean {
    private @NotEmpty String id;
    public ExampleBean(String id) {
      this.id = id;
    }
  }
  
  public void test() {
    ExampleBean bean = new ExampleBean("");
    Validator validator = XPagesValidationUtil.constructXPagesValidator();
    Set<ConstraintViolation<ExampleBean>> violations = XPagesValidationUtil.validate(bean, validator);
    if(!violations.isEmpty()) {
      // Handle error message here
    }
  }
}
```

### Implementation Details

Using Bean Validation 3.0 requires also having Expression Language 4.0 installed, even if it is not active for the current application.

## JSON-P and JSON-B

The [Java API for JSON Processing](https://jakarta.ee/specifications/jsonp/2.0/) spec is the standardized JSON library for Jakarta EE. The lack of a standard API led to the proliferation of similar-but-incompatible libraries like the initial json.org implementation, Google Gson, and (mostly for XPages developers) the IBM Commons JSON implementation. JSON-P is intended to be a simple and functional unified implementation.

The [Java API for JSON Binding](https://jakarta.ee/specifications/jsonb/2.0/) spec is a standardization of JSON serialization of Java objects, something that libraries like Gson specialize in. It allows for converting objects to and from a JSON representation, either with its default guesses or by customizing the processor or annotating the Java class.

The "org.openntf.xsp.jsonapi" library provides both of these libraries to XPages, though they don't replace any standard behavior in the environment. To avoid permissions problems, it contains an `org.openntf.xsp.jsonapi.JSONBindUtil` class to serialize and deserialize objects in `AccessController` blocks:

```java
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import org.openntf.xsp.jsonapi.JSONBindUtil;

public class JsonTest {
	public static class TestBean {
		private String firstName;
		private String lastName;
		
		public TestBean() { }
		public String getFirstName() { return firstName; }
		public void setFirstName(String firstName) { this.firstName = firstName; }
		public String getLastName() { return lastName; }
		public void setLastName(String lastName) { this.lastName = lastName; }
	}
	
	public String getJson() {
		TestBean foo = new TestBean();
		foo.setFirstName("foo");
		foo.setLastName("fooson");
		Jsonb jsonb = JsonbBuilder.create();
		return JSONBindUtil.toJson(foo, jsonb);
	}
	
	public Object getObject() {
		Jsonb jsonb = JsonbBuilder.create();
		String json = getJson();
		return JSONBindUtil.fromJson(json, jsonb, TestBean.class);
	}
}

```

## Requirements

- Domino FP10+
- Designer FP10+ (for compiling the NSF)
- Some of the APIs require setting the project Java compiler level to 1.8

## Building

To build this application, first `package` the `osgi-deps` Maven project, which will provide the target platform dependencies used by the `eclipse` Maven tree.

Additionally, set the `notes-platform` Maven property to a URI referencing an update site generated by the [`generate-domino-update-site` Maven plugin](https://github.com/OpenNTF/generate-domino-update-site). Note: this site must be generated by version 3.2.0 or newer of that plugin and must have been generated from either a Domino server or a Windows Notes client, as it requires the "xsp.http.bootstrap.jar" file present only in those installations.

## Known Issues

If your Domino Java classpath has any invalid entries in it, the CDI portion of the tooling will complain and fail to load, which may cause XPages apps generally to throw an error 500.

The workaround for this is to check your classpath (jvm/lib/ext and ndext, primarily) for any files that the Domino process user can't access (usually the local system on Windows, or `notes` on Linux). Additionally, look for a `JavaUserClassesExt` entry in the server's notes.ini and make sure that all of the files or directories it references exist and are readable.

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under compatible licenses - see NOTICE for details.