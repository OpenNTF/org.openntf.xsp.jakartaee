# XPages Jakarta EE Support

This project adds partial support for several Java/Jakarta EE technologies to XPages applications. Of the [list of technologies](https://jakarta.ee/specifications/) included in the full Jakarta EE spec, this project currently provides:

- [Servlet 6.0](#servlets) (partial)
- [Expression Language 5.0](#expression-language)
- [Contexts and Dependency Injection 4.0](#cdi)
    - Annotations 2.1
    - Interceptors 2.1
    - Dependency Injection 2.0
- [RESTful Web Services (JAX-RS) 3.1](#restful-web-services)
- [Validation 3.0](#validation)
- [JSON Processing 2.1](#json-p-and-json-b)
- [JSON Binding 3.0](#json-p-and-json-b)
- [XML Binding 4.0](#xml-binding)
- [Mail 2.1](#mail)
    - Activation 2.1
- [Concurrency 3.0](#concurrency)
- [Transactions 2.0](#transactions) (partial)
- [Data 1.0 RC1 and NoSQL 1.0 M1](#data-and-nosql)
- [Persistence 3.1](#persistence-jpa)
- [Server Pages 3.1](#server-pages-and-jstl)
- [Server Faces 4.0](#server-faces)
- [MVC 2.1](#mvc)

It also provides components from [MicroProfile](https://microprofile.io/):

- [Config 3.1](#microprofile-config)
- [Rest Client 3.0](#microprofile-rest-client)
- [Fault Tolerance 4.0](#microprofile-fault-tolerance)
- [Health 4.0](#microprofile-health)
- [OpenAPI 3.1](#microprofile-openapi)
- [Metrics 5.1](#microprofile-metrics)

These specifications are divided into three groups: core, UI, and MicroProfile.

## Core

The "org.openntf.xsp.jakartaee.core" library provides all of the specs designed for extending XPages or writing RESTful web services.

Note: the term "core" is distinct from the ["Jakarta EE Core Profile"](https://jakarta.ee/specifications/), which is a restrictive subset meant for small deployments. This feature contains many specs that aren't part of that profile.

### CDI

The [Jakarta Contexts and Dependency Injection 3.0](https://jakarta.ee/specifications/cdi/) specification provides for managed beans and dependency injection.

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
	Database database;
	
	@Inject
	@Named("dominoSession")
	Session session;
	
	@Inject
	@Named("dominoSessionAsSigner")
	Session sessionAsSigner;
	
	@Inject
	@Named("dominoSessionAsSignerWithFullAccess")
	Session sessionAsSignerWithFullAccess;
```

#### Conversation Scope

This implementation maps CDI `@ConversationScoped` beans to the XPages view scope. This isn't necessarily a [direct analogue](https://stackoverflow.com/questions/7788430/how-does-jsf-2-conversationscope-work), but it's close enough.

#### Limitations

Currently, the CDI environment for the application acts as if there is a `META-INF/beans.xml` file with `bean-discovery-mode="all"` set, but only resolves within the active NSF. So, while NSF beans and classes can reference each other, plugin and system classes are not available for CDI injection.

### Expression Language

The [Expression Language](https://jakarta.ee/specifications/expression-language/) spec is the evolved version of the original Expression Language as used in XPages. It contains numerous improvements over its predecessors, such as method parameters and [lambda expressions](http://www.baeldung.com/jsf-expression-language-el-3).

When the core library is enabled, the Jakarta EL processor takes over for all normal expression language bindings and so can be used without a prefix in some cases:

```xml
<xp:text value="${someBean.calculateFoo('some arg')}"/>
```

Note that Designer attempts to validate the syntax of runtime EL bindings; to work around this, add an "el:" prefix to the binding. This will leave a warning in Designer, but it will work:

```xml
<xp:text value="#{el:someBean.hello()}"/>
```

#### Overriding the Prefix

If you don't want Jakarta EL to take over the default handling of EL in the app, you can specify a different prefix using the `org.openntf.xsp.el.prefix` in your app's Xsp Properties file. For example:

```properties
xsp.library.depends=org.openntf.xsp.jakartaee.core
org.openntf.xsp.el.prefix=ex
```

Note that Designer refuses to compile XPages with runtime-bound expressions that use a number in the prefix, so this should be letters only.

Regardless of whether or not you specify an alternate prefix, the original XPages EL parser will be available by using expressions like `#{xspel:someBean}`.

#### Implementation Details

The Jakarta EL handler is currently stricter about null values than the default handler in XPages. For example, take this binding:

```xml
<xp:text value="${beanThatDoesNotExist.someProp}"/>
```

In standard XPages, this will result in an empty output. With the EL 4 resolver, however, this will cause an exception like `ELResolver cannot handle a null base Object with identifier 'beanThatDoesNotExist'`. I'm considering changing this behavior to match the XPages default, but there's also some value in the strictness, especially because the exception is helpful in referencing the object it's trying to resolve against, which could help track down subtle bugs.

Additionally, if you use Server JavaScript bindings inside a theme (e.g. `rendered="${javascript:...}"`), the resolver will complain on the server console if there is whitespace immediately following `javascript:`.

### Servlets

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

#### web.xml

It also adds preliminary support for web.xml, currently in the form of only supporting context parameters and Servlet definitions. For example, this file can be stored in the NSF in WebContext/WEB-INF/web.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
	  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	  xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_5_0.xsd"
	  version="5.0" metadata-complete="false">
	
	<context-param>
		<param-name>org.openntf.example.param</param-name>
		<param-value>I am the param value</param-value>
	</context-param>
	
	<servlet>
		<servlet-name>webxml-servlet</servlet-name>
		<servlet-class>servlet.WebXmlServlet</servlet-class>
		<init-param>
			<param-name>initGuy</param-name>
			<param-value>I was set by web.xml</param-value>
		</init-param>
	</servlet>
	<servlet-mapping>
		<servlet-name>webxml-servlet</servlet-name>
		<url-pattern>/webXmlServlet</url-pattern>
	</servlet-mapping>
</web-app>
```

When present, this will cause the "org.openntf.example.param" parameter to be set and available for all Jakarta users of `ServletContext`, such as Servlets, REST, Pages, and Faces. Additionally, it will map the `servlet.WebXmlServlet` class to `/foo.nsf/xsp/webXmlServlet`.

Servlets annotated with `@WebServlet` or CDI annotations like `@ApplicationScoped` will be created via CDI, allowing for the use of `@Inject` in their definitions.

Such parameters can also be defined in META-INF/web-fragment.xml files inside JARs in the NSF.

### RESTful Web Services

The [RESTful Web Services](https://jakarta.ee/specifications/restful-ws/) specification is the standard way to provide web services in Java EE applications. A version of it has been included for a long time in Domino by way of the Extension Library. However, this version is also out of date, with Apache Wink implementing JAX-RS 1.1.1.

This library is based on [the work of Martin Pradny](https://www.pradny.com/2017/11/using-jax-rs-inside-nsf.html) and provides REST support by way of [RESTEasy](https://resteasy.github.io) for classes inside the NSF. When a class is or has a method annotated with `@Path`, it is included as a service beneath `/xsp/app` inside the NSF. For example:

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

The path within the NSF can be modified by setting the `org.openntf.xsp.jakarta.rest.path` property in the NSF's "xsp.properties" file. The value there will be appended to `/xsp`. For example, setting it to `foo` will make the above example available at `/some.nsf/xsp/foo/sample`.

When converting objects to JSON, this will use [JSON-B](#json-p-and-json-b) to stream the response to the client. This streaming can be disabled in favor of pre-buffering for troubleshooting purposes by setting this in xsp.properties:

```properties
rest.jsonb.stream=false
```

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

#### CORS

CORS can be enabled and customized for REST services by enabling the MicroProfile feature (described below) and setting some or all of the following properties in the app's Xsp Properties:

```
rest.cors.enable=true                   # required for CORS
rest.cors.allowCredentials=true         # defaults to true
rest.cors.allowedMethods=GET,HEAD       # defaults to all
rest.cors.allowedHeaders=Some-Header    # defaults to all
rest.cors.exposedHeaders=Some-Header    # optional
rest.cors.maxAge=600                    # optional
# allowedOrigins is required, and can be "*"
rest.cors.allowedOrigins=http://foo.com,http://bar.com
```

### Validation

The [Validation](https://jakarta.ee/specifications/bean-validation/) spec provides a standard mechanism for performing validation of arbitrary objects via annotations. XPages doesn't provide any type of bean validation mechanism - the closest things it provides are UI component validators, but those don't connect to the back-end objects at all.

This library provides validation annotations and a processor via [Hibernate Validator](http://hibernate.org/validator/). Since there is no existing structure to hook into in the XPages runtime, bean validation must be called explicitly by your code, such as in a common "save" method in model objects. This is done by constructing a `Validator` object using the builder and then running it against a given bean. Due to the intricacies of the XPages runtime, code performing validation should be run from an OSGi plugin.

For generic use, this library provides an `org.openntf.xsp.jakarta.validation.XPagesValidationUtil` class with methods to construct a `Validator` object and to use that validator to validate a bean:

```java
import org.openntf.xsp.jakarta.validation.XPagesValidationUtil;
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

### JSON-P and JSON-B

The [Java API for JSON Processing](https://jakarta.ee/specifications/jsonp/) spec is the standardized JSON library for Jakarta EE. The lack of a standard API led to the proliferation of similar-but-incompatible libraries like the initial json.org implementation, Google Gson, and (mostly for XPages developers) the IBM Commons JSON implementation. JSON-P is intended to be a simple and functional unified implementation.

The [Java API for JSON Binding](https://jakarta.ee/specifications/jsonb/) spec is a standardization of JSON serialization of Java objects, something that libraries like Gson specialize in. It allows for converting objects to and from a JSON representation, either with its default guesses or by customizing the processor or annotating the Java class. This support is provided via [Eclipse Yasson](https://projects.eclipse.org/projects/ee4j.yasson).

To avoid permissions problems, this contains an `org.openntf.xsp.jakarta.json.JSONBindUtil` class to serialize and deserialize objects in `AccessController` blocks:

```java
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

org.openntf.xsp.jakarta.json.JSONBindUtil`;

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
		
		// {"firstName":"foo","lastName":"fooson"}
		return JSONBindUtil.toJson(foo, jsonb);
	}
	
	public Object getObject() {
		// Also injectable via CDI
		Jsonb jsonb = CDI.current().select(Jsonb.class).get();
		String json = getJson();
		return JSONBindUtil.fromJson(json, jsonb, TestBean.class);
	}
}
```

By default, JSON-B will export object properties based on publicly-visible getters (e.g. `getFoo()`) and will ignore non-public fields (e.g. `private String foo`). This behavior can be customized with a `PropertyVisibilityStrategy` object, which in turn can be passed to a `JsonbConfig` class. This configuration can be supplied via CDI, in which case it will take effect for CDI-injected `Jsonb` instances as well as in REST requests. See [the `nsf-jakartaee-jsonbconfig-example` NSF](eclipse/nsfs/nsf-jakartaee-jsonbconfig-example/odp/Code/Java/bean/JsonbConfigProvider.java) for an example of this configuration.

### XML Binding

The [XML Binding](https://jakarta.ee/specifications/xml-binding/3.0/) spec provides for translating objects to and from XML, similar to JSON-B for JSON. In addition to default translations for un-configured objects, this allows for annotating classes with information to specify their translation.

For example:

```java
@XmlRootElement(name="application-guy")
public class ApplicationGuy {
	@XmlElement(name="time")
	private final long time = System.currentTimeMillis();
	
	/* snip */
}
```

The `JaxbUtil` class provides a convenience method for creating a `JAXBContext` object to manually marshal (serialize) and unmarshal (deserialize) objects to and from XML. Additionally, objects returned in REST methods marked as emitting XML will use this implicitly:

```java
@GET
@Path("/xml")
@Produces(MediaType.APPLICATION_XML)
public Object xml() {
	// <application-guy>
	//   <time>1674221690372</time>
	// </application-guy>
	return applicationGuy;
}
```

XML Binding is provided by [EclipseLink](https://eclipse.dev/eclipselink/), which can be pickier than other implementations. Specifically, a getter annotated with `@XmlElementRef` will likely require a corresponding setter or else result in a `JAXBException` with a null message.

### Mail

The [Mail](https://jakarta.ee/specifications/mail/2.1/) API provides classes for working with MIME and other email technologies. Though this does not integrate with Domino's mail capabilities, it can be useful for generating and processing MIME messages generally. For example:

```java
@GET
public Object getMultipart() throws MessagingException {
	MimeMultipart result = new MimeMultipart();
	MimeBodyPart part = new MimeBodyPart();
	part.setContent("i am content", "text/plain");
	result.addBodyPart(part);
	result.setPreamble("I am preamble");
	return result;
}
```

This will emit content like:

```
I am preamble
------=_Part_0_9550202.1674222767917

i am content
------=_Part_0_9550202.1674222767917--
```

### Concurrency

The [Concurrency API](https://jakarta.ee/specifications/concurrency/) provides a mechanism for locating and using managed variants of `ExecutorService` and `ScheduledExecutorService` to use contextual application services from within a multithreaded context. These objects can be retrieved using JNDI:

```java
ManagedExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
ManagedScheduledExecutorService scheduler = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
```

...or using CDI:

```java
@Inject @Named("java:comp/DefaultManagedExecutorService")
private ManagedExecutorService exec;

@Inject @Named("java:comp/DefaultManagedScheduledExecutorService")
private ManagedScheduledExecutorService scheduler;
```

Tasks run from these executors will retain their NSF and requesting user context as well as the application's CDI container.

The mechanics of the pool can be configured with several xsp.properties values, with these defaults:

```properties
concurrency.hungTaskThreshold=0
concurrency.longRunningTasks=true
concurrency.corePoolSize=5
concurrency.maxPoolSize=10
concurrency.keepAliveSeconds=1800
concurrency.threadLifetimeSeconds=1800
concurrency.queueCapacity=0
```

These values apply to both the normal and scheduled services.

### Transactions

The [Transactions API](https://jakarta.ee/specifications/transactions/) provides a generic way to handle resource transactions. The implementation in this project is a partial one that aims to allow for transactions in the Domino NoSQL driver implementation (see below). For example:

```java
@GET
@Produces(MediaType.APPLICATION_JSON)
@Transactional
public Map<String, Object> createExampleDocAndPersonThenFail() {
	Person person = new Person();
	person.setFirstName("At " + System.nanoTime());
	person.setLastName("Created for exampleDocAndPersonTransactionThenFail");
	person = personRepository.save(person);
	
	ExampleDoc exampleDoc = new ExampleDoc();
	exampleDoc.setTitle("I am created for exampleDocAndPersonTransactionThenFail at " + System.nanoTime());
	exampleDoc = repository.save(exampleDoc);
	
	throw new RuntimeException("I am intentionally failing to trigger a rollback");
}
```

In this case, neither document will actually be saved to their databases.

This implementation does not currently support JNDI referencing or transaction suspension. Though transactions are propagated across Concurrency boundaries, note that Domino transactions are thread-specific and thus should be started and committed within a single thread.


### Data and NoSQL

The [Jakarta Data](https://github.com/jakartaee/data) API provides a high-level API for dealing with underlying databases as repositories, and the [Jakarta NoSQL](https://github.com/eclipse-ee4j/nosql) API provides for semi-database-neutral object mapping for NoSQL databases in a manner similar to JPA for relational databases. Entities are defined with annotations again similar to JPA:

```java
package model;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

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

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

public interface PersonRepository extends DominoRepository<Person, String> {
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

Certain queries, namely arbitrary searches with a sort parameter, will cause the driver to create QRP-view databases on disk. These will default to a system temp directory, but this can be overridden by specifying the `Jakarta_QRPDir` notes.ini property.

#### Accessing Views

View and folder data can be accessed by writing repository methods annotated with the `org.openntf.xsp.nosql.mapping.extension.ViewEntries` and `org.openntf.xsp.nosql.mapping.extension.ViewDocuments` annotations. For example:

```java
public interface PersonRepository extends DominoRepository<Person, String> {
	@ViewDocuments(FOLDER_PERSONS)
	Stream<Person> findInPersonsFolder();
	
	@ViewEntries(VIEW_PERSONS)
	Optional<Person> findByKey(ViewQuery viewQuery);
}
```

The `@ViewDocuments` annotation will retrieve all the documents contained in the view or folder, in entry order, while `@ViewEntries` will read only the entry data. The latter is potentially much faster, but does not provide full access to the underlying documents. Rich-text items are not available, for example, though view columns are accessible by programmatic name (e.g. `$3`). The `org.openntf.xsp.nosql.mapping.extension.ViewQuery` type can be used programmatically to define a query on the view data. For example, in a REST service finding a person entry by the last-name key from the view:

```java
@Path("byViewKey/{lastName}")
@GET
@Produces(MediaType.APPLICATION_JSON)
public Person getPersonByViewKey(@PathParam("lastName") String lastName) {
	ViewQuery query = ViewQuery.query().key(lastName, true); // "true" for an exact match
	return personRepository.findByKey(query)
		.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
}
```

#### Named and Profile Documents

Named and profile documents can be retrieved with the `findNamedDocument` and `findProfileDocument` methods on `DominoRepository` instances. While documents can be readily accessed this way, it is also important to include specific special fields in your model in order to create and update them. For example, for named documents:

```java
@Entity
public class SomeNamedDoc {
	@Id
	private String documentId;
	
	@Column(DominoConstants.FIELD_NOTENAME)
	private String noteName;
	
	// If using key parameters:
	@Column(DominoConstants.FIELD_USERNAME)
	private String noteUserName;
	
	/* ... */
}
```

And for profile documents:

```java
@Entity
public class SomeProfileDoc {
	@Id
	private String documentId;
	
	@Column(DominoConstants.FIELD_PROFILENAME)
	private String profileName;
	
	// If using key parameters:
	@Column(DominoConstants.FIELD_PROFILEKEY)
	private String profileKey;
	
	/* ... */
}
```

#### Document Sources

By default, the driver assumes that documents are stored in the current database. This can be overridden by using the `org.openntf.xsp.nosql.mapping.extension.RepositoryProvider` annotation. For example:

```java
package model;

import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.RepositoryProvider;

@RepositoryProvider("names")
public interface PersonRepository extends DominoRepository<Person, String> {
	Stream<Person> findAll();
	Stream<Person> findByLastName(String lastName);
}
```

Then, create a CDI bean that can provide the desired database and a `sessionAsSigner` object (which is used for QueryResultsProcessor views), annotated with `jakarta.nosql.mapping.Database`. For example:

```java
package bean;

import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import lotus.domino.NotesException;

@RequestScoped
public class NamesRepositoryBean {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "names")
	public DominoDocumentCollectionManager getNamesManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return NotesContext.getCurrent().getSessionAsSigner().getDatabase("", "names.nsf");
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> NotesContext.getCurrent().getSessionAsSigner()
		);
	}
}
```

### Persistence (JPA)

The [Persistence](https://jakarta.ee/specifications/persistence/) API (JPA) provides access and mapping to relational databases in a managed way. This feature builds on the existing [RDBMS support in XPages](https://help.hcltechsw.com/dom_designer/9.0.1/user/wpd_data_rdbms_support.html), using the same underlying configuration for the connection pools.

Declaration of a Persistence class is done similarly to NoSQL:

```java
package model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="companies", schema="public")
public class Company {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="name", length=255)
	private String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
```

Once you've configured the JDBC connection for XPages, you can then map your class to the connection in a file named `META-INF/persistence.xml` in your NSF's classpath (e.g. added in Code/Java):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
	<persistence-unit name="JPATestProj" transaction-type="JTA">
		<class>model.Company</class>
		<jta-data-source>java:comp/env/jdbc/yourconnectionname</jta-data-source>
		<properties>
			<property name="jakarta.persistence.jdbc.url" value="java:comp/env/jdbc/yourconnectionname" />
		</properties>
	</persistence-unit>
</persistence>
```

This will map a JDBC configuration defined in `WebContent/WEB-INF/jdbc/yourconnectionname.jdbc` to the `model.Company` class.

This feature does not currently provide container-managed `EntityManager`s, but they can be built up and used explicitly. For example:

```java
package rest;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.Company;

@Path("companies")
public class CompaniesResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Company> get() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("JPATestProj");
		EntityManager em = emf.createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Company> query = cb.createQuery(Company.class);
			TypedQuery<Company> tq = em.createQuery(query);
			return tq.getResultList();
		} finally {
			em.close();
		}
	}
}
```

## UI

The "org.openntf.xsp.jakartaee.ui" library contains specs useful for creating server-rendered user interfaces.

### Server Pages and JSTL

[Jakarta Server Pages](https://jakarta.ee/specifications/pages/) is the current form of the venerable JSP and provides the ability to write single-execution pages in the NSF with a shared CDI space. The [Jakarta Standard Tag Library](https://jakarta.ee/specifications/tags/) is the standard set of tags and functions available for looping, formatting, escaping, and other common operations.

When this library is enabled, .jsp files in the "Files" or "WebContent" parts of the NSF will be interpreted as live pages. For example:

```jsp
<%@page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>
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

During page compilation, the runtime will create files on disk. These will default to a system temp directory, but this can be overridden by specifying the `Jakarta_TempDir` notes.ini property.

Additionally, this process requires deploying some DTDs to the filesystem. By default, these are deployed to (Domino data)/domino/jakarta, but this can be overridden by specifying the `Jakarta_DTDDir` notes.ini property.

### Server Faces

[Jakarta Server Faces](https://jakarta.ee/specifications/faces/) is the Jakarta EE form of JSF, the spec XPages forked off from.

Faces is implemented here by way of [Apache MyFaces](https://myfaces.apache.org/).

A Faces page, like an XPage, is an XML document that is parsed and converted into components for rendering. When this library is enabled, .jsf and .xhtml files in the "Files" or "WebContent" parts of the NSF will be interpreted as live pages. For example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<f:view xmlns="http://www.w3.org/1999/xhtml"
      xmlns:f="jakarta.faces.core"
      xmlns:h="jakarta.faces.html">
	
    <h:head>
        <title>Faces Hello World</title>
    </h:head>
    <h:body>
    	<h2>Faces Hello World Example - hello.xhtml</h2>
    	
    	<dl>
    		<dt>facesContext</dt>
    		<dd><h:outputText value="#{facesContext}"/></dd>
    		
    		<dt>requestGuy.message</dt>
    		<dd><h:outputText value="#{requestGuy.message}"/></dd>
    		
    		<dt>Project Stage</dt>
    		<dd><h:outputText value="#{facesContext.application.projectStage}"/></dd>
    	</dl>
    	
    	<fieldset>
    		<legend>Example Form</legend>
    		<h:form>
    			<h:inputText id="appGuyProperty" value="#{applicationGuy.beanProperty}"/>
    			<h:commandButton value="Refresh">
    				<f:ajax execute="appGuyProperty" render="formOutput"/>
    			</h:commandButton>
    			
    			<p><h:outputText id="formOutput" value="#{applicationGuy.beanProperty}"/></p>
    		</h:form>
    	</fieldset>
    </h:body>
</f:view>
```

#### Development Mode

The "Project Stage" value can be set in the Xsp Properties file to one of the values from `jakarta.faces.application.ProjectStage`. For example:

```
jakarta.faces.PROJECT_STAGE=Development
```

This is useful to alter internal behaviors and optimizations. For example, setting Development there will cause the runtime to less-heavily cache page definitions.

#### ClassLoaders

Because Faces classes and lifecycle registrations will often conflict with XPages elements, it's not possible to add extensions like [PrimeFaces](https://primefaces.org) as a normal JAR within the application. To work around this, you can put JARs in WebContent/WEB-INF/jakarta/lib - these will be loaded in the Faces class loader, but won't be present in the normal Java classpath of the NSF. Faces UI libraries placed here can be used inside XHTML files, though

Additionally, if you want to use XPages and Faces extension components in faces-config.xml within the same app, you can create a second file, WebContent/WEB-INF/jakarta/faces-config.xml, to house the Faces components. When present, it will be used in preference to the normal file for Faces requests.

### MVC

The [Jakarta MVC](https://jakarta.ee/specifications/mvc/) specification allows for action-based MVC using Jakarta REST as the controller layer and (by default) Pages as the view layer. With this, you can annotate a REST resource or method with `@Controller`, perform setup actions, and then return the name of a page to render. For example:

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

@Path("mvc")
@Controller
@RequestScoped
public class MvcExample {
	
	@Inject
	Models models;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("foo") String foo) {
		models.put("incomingFoo", foo);
		return "mvc.jsp";
	}
}
```

This will load the JSP file stored as `WebContent/WEB-INF/views/mvc.jsp` in the NSF and evaluate it with the values from "models" and CDI beans available for use.

## MicroProfile

The "org.openntf.xsp.microprofile" library contains all of the available MicroProfile specs, which enhance the capabilities of the core platform, particularly when writing RESTful web services.

### MicroProfile Config

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

### MicroProfile Rest Client

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

### MicroProfile Fault Tolerance

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

### MicroProfile Health

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

### MicroProfile OpenAPI

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

### MicroProfile Metrics

Using [MicroProfile Metrics](https://github.com/eclipse/microprofile-metrics), it is possible to track invocations and timing from REST services. For example:

```java
@GET
@Timed
public Response hello() {
	/* Perform the work */
}
```

When such a service is executed, its performance is logged and becomes available via `/xsp/app/metrics` within the NSF:

```
# TYPE application_rest_Sample_hello_total counter
application_rest_Sample_hello_total 2.0
# TYPE application_rest_Sample_hello_elapsedTime_seconds gauge
application_rest_Sample_hello_elapsedTime_seconds 8.252E-4
```

This capability can be disabled by setting `rest.mpmetrics.enable=false` in your Xsp Properties. Note that Fault Tolerance has an implicit dependency on this, and so will also be unavailable if you set this flag.

## Requirements

- Domino 14+
- Designer 14+ (for compiling the NSF)

NoSQL and the MicroProfile Rest Client require loosening Domino's java.policy settings to include:

```
grant {
	permission java.security.AllPermission;
};
```

If this is unset, problems with NoSQL will manifest with root exceptions like `jakarta.nosql.ProviderNotFoundException: Provider not found: interface jakarta.nosql.document.DocumentQueryParser`.

## Building

Building requires that Maven run with Java 17 or above.

To build this application, first `package` the `osgi-deps` Maven project, which will provide the target platform dependencies used by the `eclipse` Maven tree.

Additionally, set the `notes-platform` Maven property to a URI referencing an update site generated by the [`generate-domino-update-site` Maven plugin](https://github.com/OpenNTF/generate-domino-update-site). Note: this site must be generated by version 4.2.1 or newer of that plugin and must have been generated from either a Domino server or a Windows Notes client, as it requires the "xsp.http.bootstrap.jar" file present only in those installations.

Finally, you should have a Maven toolchain configured that provides JavaSE-17. For example, you could have a ~/.m2/toolchains.xml file like this (for a Temurin 17 installation on macOS):

```xml
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">
   <toolchain>
      <type>jdk</type>
      <provides>
         <id>JavaSE-17</id>
      </provides>
      <configuration>
         <jdkHome>/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home</jdkHome>
      </configuration>
   </toolchain>
</toolchains>
```

## Known Issues

If your Domino Java classpath has any invalid entries in it, the CDI portion of the tooling will complain and fail to load, which may cause XPages apps generally to throw an error 500.

The workaround for this is to check your classpath (ndext, primarily) for any files that the Domino process user can't access (usually the local system on Windows, or `notes` on Linux). Additionally, look for a `JavaUserClassesExt` entry in the server's notes.ini and make sure that all of the files or directories it references exist and are readable.

See [COMPATIBILITY.md](COMPATIBILITY.md) for details on known incompatibilities with specific projects.

## License

The code in the project is licensed under the Apache License 2.0. The dependencies in the binary distribution are licensed under compatible licenses - see NOTICE for details.
