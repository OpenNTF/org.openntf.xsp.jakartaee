# Unit and Integration Tests

The test suites in this project are broken up into two main categories: Tycho-run tests and Docker-based integration tests.

## Tycho-Run Tests

The Tycho-run tests (e.g. `tests/org.openntf.xsp.beanvalidation.test`) are intended to perform checks to ensure that components work in the abstract in an OSGi environment, but without an active Notes runtime. This is useful for ensuring that, for example, extension point registrations and providers are functional.

These tests run automatically during a build and use the same update site that the main compilation process does.

## Integration Tests

The `tests/it-xsp-jakartaee` module uses [Testcontainers](https://www.testcontainers.org) to configure and run a Domino container to test proper loading and execution of most components.

#### Requirements

The requirements to run this test are:

- An available local or remote Docker runtime (remote is untested, but _should_ work)
- An installed version of the official Domino container image, or compatible

The default container base image is `domino-container:V1202_11032022prod`, the 12.0.2 release. This can be overridden by setting the Java property `jakarta.baseImage` to a new image name when running tests

#### Building For Tests

The IT suite uses compiled and packaged versions of the final update site and the NSFs in the `nsfs` directory, and so those must be built and installed via a Maven Install for the tests to execute. The NSFs are built using the [NSF ODP Tooling](https://github.com/OpenNTF/org.openntf.nsfodp) project, and so it is required to have that set up and working properly.

#### Relation To nsf-jakartaee-example

The test suites use the nsf-jakartaee-example project as the basis for most of their tests, and so changes to that ODP may affect test cases. Usually, adding new examples doesn't break tests, but existing ones may require changes to case assertions for content, paths, etc..

#### Running

The `it-xsp-jakartaee` module and its tests can be run during a build by activiating the `run-it` Maven profile. Alternatively, the tests are designed to not require special environment setup beyond the above requirements and can be run from a standalone JUnit runner, such as Eclipse.