# Custom Dependency Injection

Java implementation of a custom dependency injection mechanism.

### Limitations

1. Doesn't support classes that implement multiple interfaces.
2. Doesn't support constructor annotation with @CustomAutowired.
3. Assumes DIMapWrapper's package as the root. An alternative is to read root package from a property file.