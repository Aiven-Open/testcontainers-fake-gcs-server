testcontainers-fake-gcs-server
======================
Testcontainers wrapper for [https://github.com/fsouza/fake-gcs-server](https://github.com/fsouza/fake-gcs-server).

Usage
=====

1. Add the dependency:

```xml
<dependency>
  <groupId>io.aiven</groupId>
  <artifactId>testcontainers-fake-gcs-server</artifactId>
  <version>0.1.0</version>
</dependency>
```

2. Use in tests with JUnit 5 and Testcontainers:

```java
@Testcontainers
class MyTest {
    @Container
    private static final FakeGcsServerContainer FAKE_GCS_SERVER_CONTAINER =
        new FakeGcsServerContainer();

    @Test
    void test() {
        final Storage storage = StorageOptions.newBuilder()
            .setCredentials(NoCredentials.getInstance())
            .setHost(FAKE_GCS_SERVER_CONTAINER.url())
            .setProjectId("test-project")
            .build()
            .getService();

        storage.create(BucketInfo.of("test-bucket"));
    }
}
```

License
============
`testcontainers-fake-gcs-server` is licensed under the Apache license, version 2.0. Full license text is available in the [LICENSE](LICENSE) file.

Please note that the project explicitly does not require a CLA (Contributor License Agreement) from its contributors.

Contact
============
Bug reports and patches are very welcome, please post them as GitHub issues and pull requests at [https://github.com/Aiven-Open/testcontainers-fake-gcs-server](https://github.com/Aiven-Open/testcontainers-fake-gcs-server). 
To report any possible vulnerabilities or other serious issues please see our [security](SECURITY.md) policy.
