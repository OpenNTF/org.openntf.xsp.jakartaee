# XPages Jakarta EE Support

This project adds partial support for several Java/Jakarta EE technologies to XPages applications. Of the [list of technologies](https://jakarta.ee/specifications/) included in the full Jakarta EE spec, this project currently provides:

- Servlet 5.0 (Partial)
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
- Server Pages 3.0
- Server Faces 4.0 (snapshot)
- MVC 2.0
- NoSQL 1.0 (snapshot)

It also provides components from [MicroProfile](https://microprofile.io/):

- OpenAPI 3.0
- Rest Client 3.0
- Config 3.0
- Metrics 4.0
- Fault Tolerance 4.0
- Health 4.0

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

The contextual Domino objects - the `Database` and `Session`s - are available to use with `@Inject`. In the case of `Database`, this can be used without any modifiers. In the case of the `Session`s, they are available with `@Named` qualifiers:

```java
	@Inject
	@Named("dominoClient")
	Session session;
	
	@Inject
	@Named("dominoClientAsSigner")
	Session sessionAsSigner;
	
	@Inject
	@Named("dominoClientAsSignerWithFullAccess")
	Session sessionAsSignerWithFullAccess;
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

## Servlets

This project adds support for specifying Servlets in an NSF using the `@WebServlet` annotation. For example:

```java
package servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/someservlet", "/someservlet/*", "*.hello" })
public class ExampleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello from ExampleServlet. context=" + req.getContextPath() + ", path=" + req.getServletPath() + ", pathInfo=" + req.getPathInfo());
		resp.getWriter().flush();
	}
}
```

These Servlets will be available under `/xsp` in the NSF with matching patterns. For example, the above Servlet will match `/foo.nsf/xsp/someservlet`, `/foo.nsf/xsp/someservlet/bar`, and `/foo.nsf/xsp/testme.hello`.

These Servlets participate in the XPages lifecycle and have programmatic access to CDI beans via `CDI.current()`.

Note, however, that other Servlet artifacts such as `@WebFilter` and `@WebListener` are not yet supported.

## RESTful Web Services

The [RESTful Web Services](https://jakarta.ee/specifications/restful-ws/3.0/) specification is the standard way to provide web services in Java EE applications. A version of it has been included for a long time in Domino by way of the Extension Library. However, this version is also out of date, with Apache Wink implementing JAX-RS 1.1.1.

This library is based on [the work of Martin Pradny](https://www.pradny.com/2017/11/using-jax-rs-inside-nsf.html) and provides JAX-RS 3.0 support by way of [RESTEasy 6.0](https://resteasy.github.io) for classes inside the NSF. When a class is or has a method annotated with `@Path`, it is included as a service beneath `/xsp/app` inside the NSF. For example:

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

The path within the NSF can be modified by setting the `org.openntf.xsp.jaxrs.path` property in the NSF's "xsp.properties" file. The value there will be appended to `/xsp`. For example, setting it to `foo` will make the above example available at `/some.nsf/xsp/foo/sample`.

#### Security

REST resources can be individually secured with the `@RolesAllowed` annotation. Values in this annotation are matched against the user's effective names list: their username, various permutations, their groups, and their DB-specific roles. For example:

```java
@GET
@RolesAllowed({ "*/O=SomeOrg", "LocalDomainAdmins", "[Admin]" })
public Object get() {
	// ...
}
```

Additionally, the special pseudo-name "login" can be used to require that the user be logged in at all, but not restrict to specific users beyond that.

#### OpenAPI

Using [MicroProfile OpenAPI](https://github.com/eclipse/microprofile-open-api), these REST services are also made available via `/xsp/app/openapi` within the NSF. This resource includes information about each available REST endpoint in the NSF and will produce YAML by default and JSON upon request via an `Accept` header. Additionally, `/xsp/app/openapi.yaml` and `/xsp/app/openapi.json` are available to produce YAML and JSON explicitly without consulting the `Accept` header.

Moreover, resources can be [annotated with the MicroProfile OpenAPI annotations](https://openliberty.io/guides/microprofile-openapi.html). For example:

```java
@GET
@Operation(
	summary = "Example service",
	description = "Returns an object that says 'hello' to you"
)
public Response hello() {
	// ...
}
```

#### Metrics

Using [MicroProfile Metrics](https://github.com/eclipse/microprofile-metrics), it is possible to track invocations and timing from REST services. For example:

```java
@GET
@Timed
public Response hello() {
	/* Perform the work */
}
```

When such a service is executed, its performance is logged and becomes available via `/xsp/app/metrics` within the NSF.

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

## JSP and JSTL

[Jakarta Server Pages](https://jakarta.ee/specifications/pages/3.0/) is the current form of the venerable JSP and provides the ability to write single-execution pages in the NSF with a shared CDI space. The [Jakarta Standard Tag Library](https://jakarta.ee/specifications/tags/2.0/) is the standard set of tags and functions available for looping, formatting, escaping, and other common operations.

When this library is enabled, .jsp files in the "Files" or "WebContent" parts of the NSF will be interpreted as live pages. For example:

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
	<head>
		<title>JSP Inside An NSF</title>
	</head>
	<body>
		<p>My CDI Bean is: ${applicationGuy}</p>
		<p>My requestScope is: ${requestScope}</p>
		<p>JSTL XML-escaped content is: ${fn:escapeXml('<hello>')}</p>
		
		<t:example value="Value sent into the tag"/>
	</body>
</html>
```

As demonstrated above, this will resolve in-NSF tags via the NSF's classpath and will allow the use of CDI beans.

## Server Faces 4.0

[Jakarta Server Faces](https://jakarta.ee/specifications/faces/4.0/) is the in-development next form of JSF, the spec XPages forked off from. Version 4.0 of the spec, used here, is in the final stages of development and focuses on removing legacy features and better integrating with other components (such as CDI).

JSF is implemented here by way of [Apache MyFaces](https://myfaces.apache.org/#/core40).

A Faces page, like an XPage, is an XML document that is parsed and converted into components for rendering. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<f:view xmlns="http://www.w3.org/1999/xhtml"
      xmlns:f="jakarta.faces.core"
      xmlns:h="jakarta.faces.html">
	
    <h:head>
        <title>JSF 4.0 Hello World</title>
    </h:head>
    <h:body>
	    	<h2>JSF 4.0 Hello World Example - hello.xhtml</h2>
	    	
	    	<dl>
	    		<dt>facesContext</dt>
	    		<dd><h:outputText value="#{facesContext}"/></dd>
	    		
	    		<dt>requestGuy.message</dt>
	    		<dd><h:outputText value="#{requestGuy.message}"/></dd>
	    		
	    		<dt>Project Stage</dt>
	    		<dd><h:outputText value="#{facesContext.application.projectStage}"/></dd>
	    	</dl>
    </h:body>
</f:view>
```

The "Project Stage" value can be set in the Xsp Properties file to one of the values from `jakarta.faces.application.ProjectStage`. For example:

```
jakarta.faces.PROJECT_STAGE=Development
```

This is useful to alter internal behaviors and optimizations. For example, setting Development there will cause the runtime to less-heavily cache page definitions.

## MVC

The [Jakarta MVC](https://jakarta.ee/specifications/mvc/2.0/) specification allows for action-based MVC using JAX-RS as the controller layer and (by default) JSP as the view layer. With this, you can annotate a JAX-RS resource or method with `@Controller`, perform setup actions, and then return the name of a page to render. For example:

```java
package servlet;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.NotesException;

@Path("mvc")
@Controller
@RequestScoped
public class MvcExample {
	
	@Inject
	Models models;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("foo") String foo) throws NotesException {
		models.put("incomingFoo", foo);
		return "mvc.jsp";
	}
}
```

This will load the JSP file stored as `WebContent/WEB-INF/views/mvc.jsp` in the NSF and evaluate it with the values from "models" and CDI beans available for use.

## NoSQL

The [Jakarta NoSQL](https://github.com/eclipse-ee4j/nosql) API provides for semi-database-neutral object mapping for NoSQL databases in a manner similar to JPA for relational databases. Entities are defined with annotations again similar to JPA:

```java
package model;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class Person {
	@Id
	private String unid;
	
	@Column("FirstName")
	private String firstName;
	
	@Column("LastName")
	private String lastName;

	public String getUnid() { return unid; }
	public void setUnid(String unid) { this.unid = unid; }

	public String getFirstName() { return firstName; }
	public void setFirstName(String firstName) { this.firstName = firstName;	}

	public String getLastName() { return lastName; }
	public void setLastName(String lastName) { this.lastName = lastName; }
}
```

This API builds on CDI to dynamically generate repositories based on method names and parameters. In basic cases, this can be done with no annotations or custom code at all:

```java
package model;

import java.util.stream.Stream;

import jakarta.nosql.mapping.Repository;

public interface PersonRepository extends Repository<Person, String> {
	Stream<Person> findAll();
	Stream<Person> findByLastName(String lastName);
}
```

These repositories can then be used via CDI injection, such as in a REST endpoint:

```java
// snip

@Path("nosql")
public class NoSQLExample {
	@Inject
	PersonRepository personRepository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object get(@QueryParam("lastName") String lastName) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("byQueryLastName", personRepository.findByLastName(lastName).collect(Collectors.toList()));
		result.put("totalCount", personRepository.count());
		return result;
	}
}
```

## MicroProfile Config

The [MicroProfile Config](https://github.com/eclipse/microprofile-config) API allows injection of configuration parameters from externalized sources, separating configuration from code. These parameters can then be injected using CDI. For example:

```java
@ApplicationScoped
public class ConfigExample {
	@Inject
	@ConfigProperty(name="java.version")
	private String javaVersion;
	
	@Inject
	@ConfigProperty(name="xsp.library.depends")
	private String xspDepends;
	
	/* use the above */
}
```

Four providers are currently configured:

- A system-properties source, such as "java.version"
- A source from `META-INF/microprofile-config.properties` within the NSF
- A source from `xsp.properties` in the NSF, such as "xsp.library.depends" or custom values
- A source from Domino environment variables, such as "Directory"

## MicroProfile Rest Client

The [MicroProfile Rest Client](https://github.com/eclipse/microprofile-rest-client) API allows for creation of type-safe clients for remote REST services using Jakarta REST annotations. For example:

```java
@ApplicationEScoped
public class RestClientExample {
	public static class JsonExampleObject {
		private String foo;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
	}
	
	public interface JsonExampleService {
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		JsonExampleObject get();
	}
	
	public Object get() {
		URI serviceUri = URI.create("some remote service");
		JsonExampleService service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService.class);
		JsonExampleObject responseObj = service.get();
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("called", serviceUri);
		result.put("response", responseObj);
		return result;
	}
}
```

## MicroProfile Fault Tolerance

The [MicroProfile Fault Tolerance](https://github.com/eclipse/microprofile-fault-tolerance) API allows CDI beans to be decorated with rules for handling exceptions, timeouts, and concurrency restrictions. For example:

```java
@ApplicationScoped
public class FaultToleranceBean {
	@Retry(maxRetries = 2)
	@Fallback(fallbackMethod = "getFailingFallback")
	public String getFailing() {
		throw new RuntimeException("this is expected to fail");
	}
	
	@SuppressWarnings("unused")
	private String getFailingFallback() {
		return "I am the fallback response.";
	}
	
	@Timeout(value=5, unit=ChronoUnit.MILLIS)
	public String getTimeout() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(10);
		return "I should have stopped.";
	}
	
	@CircuitBreaker(delay=60000, requestVolumeThreshold=2)
	public String getCircuitBreaker() {
		throw new RuntimeException("I am a circuit-breaking failure - I should stop after two attempts");
	}
}
```

## MicroProfile Health

The [MicroProfile Health](https://github.com/eclipse/microprofile-health) API allows you to create CDI beans that provide health checks and statistics for your application, queryable at standard endpoints. For example:

```java
package health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.ApplicationScoped;
import lotus.domino.Database;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;

@ApplicationScoped
@Liveness
public class PassingHealthCheck implements HealthCheck {
	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder response = HealthCheckResponse.named("I am the liveliness check");
		try {
			Database database = NotesContext.getCurrent().getCurrentDatabase();
			NoteCollection notes = database.createNoteCollection(true);
			notes.buildCollection();
			return response
				.status(true)
				.withData("noteCount", notes.getCount())
				.build();
		} catch(NotesException e) {
			return response
				.status(false)
				.withData("exception", e.text)
				.build();
		}
	}
}
```

In addition to `@Liveness`, Health also allows checks to be categorized as `@Readiness` and `@Startup`.

The results of these checks will be available at `/xsp/app/health` (aggregating all types), `/xsp/app/health/ready`, `/xsp/app/health/live`, and `/xsp/app/health/started`. These endpoints will emit JSON describing the applicable health checks and an overall "UP" or "DOWN" status. For example:

```json
{
    "status": "DOWN",
    "checks": [
        {
            "name": "I am the liveliness check",
            "status": "UP",
            "data": {
                "noteCount": 63
            }
        },
        {
            "name": "I am a failing readiness check",
            "status": "DOWN"
        },
        {
            "name": "started up fine",
            "status": "UP"
        }
    ]
}
```

## Requirements

- Domino FP10+
	- NoSQL requires Domino 12.0.1+
- Designer FP10+ (for compiling the NSF)
- Some of the APIs require setting the project Java compiler level to 1.8

NoSQL and the MicroProfile Rest Client require loosening Domino's java.policy settings to include:

```
grant {
	permission java.security.AllPermission;
};
```

## Building

To build this application, first `package` the `osgi-deps` Maven project, which will provide the target platform dependencies used by the `eclipse` Maven tree.

Additionally, set the `notes-platform` Maven property to a URI referencing an update site generated by the [`generate-domino-update-site` Maven plugin](https://github.com/OpenNTF/generate-domino-update-site). Note: this site must be generated by version 3.2.0 or newer of that plugin and must have been generated from either a Domino server or a Windows Notes client, as it requires the "xsp.http.bootstrap.jar" file present only in those installations.

## Known Issues

If your Domino Java classpath has any invalid entries in it, the CDI portion of the tooling will complain and fail to load, which may cause XPages apps generally to throw an error 500.

The workaround for this is to check your classpath (jvm/lib/ext and ndext, primarily) for any files that the Domino process user can't access (usually the local system on Windows, or `notes` on Linux). Additionally, look for a `JavaUserClassesExt` entry in the server's notes.ini and make sure that all of the files or directories it references exist and are readable.

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under compatible licenses - see NOTICE for details.