# XPages Jakarta EE Support

This project adds partial support for several Java/Jakarta EE technologies to XPages applications.

## CDI 1.2

The [Context and Dependency Injection for Java EE (CDI) 1.2](http://cdi-spec.org/) specification provides for managed beans and dependency injection. To use this feature, add the "org.openntf.xsp.cdi" library to your XPages app.

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

The [Expression Language 3.0](https://jcp.org/en/jsr/detail?id=341) spec is the evolved version of the original Expression Language as used in XPages. It contains numerous improvements over its predecessors, such as method parameters and [lamba expressions](http://www.baeldung.com/jsf-expression-language-el-3). To use this feature, add the "org.openntf.xsp.el3" library to your XPages app.

When the library is enabled, the EL 3 processor takes over for all normal expression language bindings and so can be used without a prefix in some cases:

```xml
<xp:text value="${dataObjectExample.calculateFoo('some arg')}"/>
```

Note that Designer attempts to validate the syntax of runtime EL bindings; to work around this, add an "el:" prefix to the binding. This will leave a warning in Designer, but it will work:

```xml
<xp:text value="#{el:requestGuy.hello()}"/>
```

## Requirements

- Domino FP8+
- Designer FP10+ (for compiling the NSF)

## Building

To build this application, first `package` the `osgi-deps` Maven project, which will provide the target platform dependencies used by the `eclipse` Maven tree.

## License

Apache License 2.0