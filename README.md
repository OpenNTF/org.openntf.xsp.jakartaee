# XPages CDI Support

This project adds partial support for the [Context and Dependency Injection for Java EE (CDI) 1.0](http://cdi-spec.org/) for XPages applications.

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

## Requirements

- Domino FP8+
- Designer FP10+ (for compiling the NSF)

## License

Apache License 2.0