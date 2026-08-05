# ARCHITECTURE OVERVIEW

The __forgepack-ARTIFACT__ is a Spring Boot auto-configuration library structured around a clear separation between public API and private implementation.

## 1. PACKAGE STRUCTURE

| Package        | Path                                          | Responsibility                                                                  |
|:---------------|:----------------------------------------------|:--------------------------------------------------------------------------------|
| __api__        | `dev.forgepack.ARTIFACT.api`            | Public contracts: interfaces, annotations, `@ConfigurationProperties`, records. |
| __internal__   | `dev.forgepack.ARTIFACT.internal`       | Private implementation: beans, services, and auto-configuration classes.        |

> **Rule:** Consumers should only reference types in `api`. The `internal` package is an implementation detail and may change between minor versions without notice.

## 2. SPRING BOOT AUTO-CONFIGURATION

The library uses Spring Boot's auto-configuration mechanism:

```
src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

The main auto-configuration class is registered there and is discovered automatically when the library JAR is on the classpath.

### 2.1. Auto-Configuration Flow

1. **Classpath detection**: Spring Boot scans `AutoConfiguration.imports` and loads the registered classes.
2. **Conditional activation**: `@ConditionalOnClass`, `@ConditionalOnMissingBean`, and `@ConditionalOnProperty` control when each bean is activated.
3. **Bean registration**: Required beans are created and exposed to the host application context.
4. **Properties binding**: `@ConfigurationProperties` classes bind values from `application.properties` / `application.yml`.

## 3. KEY DESIGN PATTERNS

### 3.1. API / Internal Separation
- `api/` contains only interfaces, annotations, records, and `@ConfigurationProperties` — no Spring beans.
- `internal/` contains all `@Bean`, `@Service`, `@Component` definitions, hidden from consumers.

### 3.2. Configuration Properties
All configurable values are bound through a strongly-typed properties class prefixed with:
```properties
forgepack.ARTIFACT.*
```

### 3.3. `@ConditionalOnMissingBean`
All auto-configured beans use `@ConditionalOnMissingBean` so consumers can override any default by declaring their own bean.

## 4. TECHNOLOGIES

| Category          | Technology                                    | Details                                              |
|:------------------|:----------------------------------------------|:-----------------------------------------------------|
| __Framework__     | Java 25, Spring Boot 4.1.0                    | Auto-configuration library starter                   |
| __Build__         | Maven 3.8+                                    | Dependency management and lifecycle                  |
| __Quality__       | JaCoCo, Surefire                              | Coverage reporting and unit test execution           |
| __Testing__       | JUnit 5, Mockito, `ApplicationContextRunner`  | Unit + auto-configuration integration tests          |
| __Distribution__  | Maven Central (`dev.forgepack`)               | Public artifact distribution                         |
