<div align="center">

# _forgepack-authentication_

[![GitHub stars](https://img.shields.io/github/stars/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication)
[![GitHub forks](https://img.shields.io/github/forks/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication/fork)
[![GitHub watchers](https://img.shields.io/github/watchers/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication)

</div>

## Tech Stack
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue?logo=apachemaven)

## Description
![GitHub last commit](https://img.shields.io/github/last-commit/forgepack/forgepack-authentication)
![Maven Central](https://img.shields.io/maven-central/v/dev.forgepack/authentication)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Test Coverage](https://img.shields.io/badge/coverage-90%25-brightgreen)

_forgepack-authentication_ is a Spring Boot auto-configuration library that {DESCRIPTION}.

## SUMMARY
- [1. Installation](#1-installation)
- [2. Usage](#2-usage)
- [3. Auto-Configuration](#3-auto-configuration)
- [4. Quality & Testing](#4-quality--testing)
- [5. Artifact Coordinates](#5-artifact-coordinates)
- [Developers](#developers)
- [License](#license)

## 1. INSTALLATION

### 1.1. Maven
```xml
<dependency>
    <groupId>dev.forgepack</groupId>
    <artifactId>authentication</artifactId>
    <version>{VERSION}</version>
</dependency>
```

### 1.2. Gradle
```groovy
implementation 'dev.forgepack:authentication:{VERSION}'
```

## 2. USAGE

### 2.1. Basic Setup

The library auto-configures itself via Spring Boot's auto-configuration mechanism. No additional `@EnableXxx` annotation is required.

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 2.2. Configuration Properties

```properties
# application.properties
forgepack.authentication.enabled=true
forgepack.authentication.property-name=value
```

## 3. AUTO-CONFIGURATION

The library registers its auto-configuration through:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

All public API classes are available under `dev.forgepack.authentication.api`.  
Internal implementation details are encapsulated in `dev.forgepack.authentication.internal`.

## 4. QUALITY & TESTING

### 4.1. Current Coverage Metrics

_Measured with JaCoCo (`mvn clean test jacoco:report`) against `src/main/java`._

GENERAL COVERAGE (lines): 90%
BRANCH COVERAGE: 86%
INSTRUCTION COVERAGE: 88%
TOTAL NUMBER OF TESTS: 85

| Package                                                       | Coverage |        |
|:---------------------------------------------------------------|:--------:|:------:|
| 📁 dev.forgepack.authentication.api                             |   n/a¹   |   ⚪   |
| 📁 dev.forgepack.authentication.internal.configuration          |   95%    |   🟢   |
| 📁 dev.forgepack.authentication.internal.configuration.filter   |   100%   |   🟢   |
| 📁 dev.forgepack.authentication.internal.controller             |   100%   |   🟢   |
| 📁 dev.forgepack.authentication.internal.mapper                 |   100%   |   🟢   |
| 📁 dev.forgepack.authentication.internal.model                  |   84%    |   🟢   |
| 📁 dev.forgepack.authentication.internal.payload                |   100%   |   🟢   |
| 📁 dev.forgepack.authentication.internal.service                |   85%    |   🟢   |
| 📁 dev.forgepack.authentication.internal.utils                  |   92%    |   🟢   |

¹ `api` contains only interface declarations (no executable bytecode to instrument).

### 4.2. Types of Tests Implemented
1. __Unit Tests (JUnit 5 + Mockito)__: services, controllers, mappers, JWT configuration/filter, cache configuration, models, DTOs and utilities, covering success paths, validation failures and exception handling
2. __Property/Record Tests__: configuration records (`JwtProperties`, `CacheProperties`) and payload records validating defaults, constraints and derived behavior

### 4.3. Running Tests
```bash
# run all tests
mvn test

# run tests and generate JaCoCo coverage report
mvn clean test jacoco:report
```

## 5. authentication COORDINATES

### 5.1. Dependency declaration
```xml
<dependency>
    <groupId>dev.forgepack</groupId>
    <artifactId>authentication</artifactId>
    <version>{VERSION}</version>
</dependency>
```

### 5.2. Plugin declaration
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>build-info</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### 5.3. Custom application properties
```properties
# ╔══════════════════════════════════════════════╗
# ║         LIBRARY CONFIGURATION                ║
# ╚══════════════════════════════════════════════╝
forgepack.authentication.enabled=true
forgepack.authentication.property-name=default-value
```

## DEVELOPERS

### Contributors
> _[Gadelha TI](https://github.com/gadelhati)_ - *Architect & Lead Developer*

## LICENSE

This project is licensed under the __MIT License__ - see the [MIT LICENSE](https://choosealicense.com/licenses/mit/) file for details.

```text
MIT License

Copyright (c) 2024 Gadelha TI

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

<div align="center">

__⭐ Did you like the project? Leave a star! ⭐__

[![GitHub stars](https://img.shields.io/github/stars/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication)
[![GitHub forks](https://img.shields.io/github/forks/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication/fork)
[![GitHub watchers](https://img.shields.io/github/watchers/forgepack/forgepack-authentication?style=social)](https://github.com/forgepack/forgepack-authentication)

__Made by [Forgepack](https://github.com/forgepack)__

</div>
