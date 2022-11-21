# Code-First REST API

This project is the example from the blog post [Code-First REST APIs With XPages Jakarta EE Support](https://frostillic.us/blog/posts/2022/8/25/code-first-rest-apis-with-xpages-jakarta-ee-support).

It demonstrates using using Jakarta NoSQL, Bean Validation, and REST to define a type-safe CRUD REST API that stores data in the current NSF. This code consists of two class files - `model.Employee` and `rest.EmployeeResource` - and defines endpoints for basic operations:

- `GET /foo.nsf/xsp/app/employees` - lists all Employee entities as a JSON array, sorted ascending by name
- `POST /foo.nsf/xsp/app/employees` - creates a new Employee entity by POSTing a valid JSON representation
- `GET /foo.nsf/xsp/app/employees/{id}` - retrieves the Employee entity with UNID `id` as a JSON object
- `PUT /foo.nsf/xsp/app/employees/{id}` - updates the Employee entity with UNID `id` with a new valid JSON representation
- `DELETE /foo.nsf/xsp/app/employees/{id}` - deletes the Employee entity with UNID `id`

This example requires no dependencies other than the XPages Jakarta EE Support project. It was written to target version 2.8.0.