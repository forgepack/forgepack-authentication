# _forgepack-ARTIFACT_
[![GitHub stars](https://img.shields.io/github/stars/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT)
[![GitHub forks](https://img.shields.io/github/forks/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT/fork)
[![GitHub watchers](https://img.shields.io/github/watchers/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT)

![GitHub last commit](https://img.shields.io/github/last-commit/forgepack/forgepack-ARTIFACT)
![Maven Central](https://img.shields.io/maven-central/v/dev.forgepack/ARTIFACT)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Test Coverage](https://img.shields.io/badge/coverage-0%25-red)

## Tech Stack
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue?logo=apachemaven)

## Description

_forgepack-ARTIFACT_ is a Spring Boot auto-configuration library that {DESCRIPTION}.

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
    <artifactId>ARTIFACT</artifactId>
    <version>{VERSION}</version>
</dependency>
```

### 1.2. Gradle
```groovy
implementation 'dev.forgepack:ARTIFACT:{VERSION}'
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
forgepack.ARTIFACT.enabled=true
forgepack.ARTIFACT.property-name=value
```

## 3. AUTO-CONFIGURATION

The library registers its auto-configuration through:

```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

All public API classes are available under `dev.forgepack.ARTIFACT.api`.  
Internal implementation details are encapsulated in `dev.forgepack.ARTIFACT.internal`.

## 4. QUALITY & TESTING

### 4.1. Current Coverage Metrics

GENERAL COVERAGE: 0%
TOTAL NUMBER OF TESTS: 0

| Package                                              | Coverage |        |
|:-----------------------------------------------------|:--------:|:------:|
| 📁 dev.forgepack.ARTIFACT.api                  |    0%    |   🔴   |
| 📁 dev.forgepack.ARTIFACT.internal             |    0%    |   🔴   |

### 4.2. Types of Tests Implemented
1. __Unit Tests__: Service and component layer
2. __Integration Tests__: Spring context loading via `@SpringBootTest`
3. __Auto-Configuration Tests__: `ApplicationContextRunner` scenarios

### 4.3. Running Tests
```bash
# run all tests
mvn test

# run tests and generate JaCoCo coverage report
mvn clean test jacoco:report
```

## 5. ARTIFACT COORDINATES

### 5.1. Dependency declaration
```xml
<dependency>
    <groupId>dev.forgepack</groupId>
    <artifactId>ARTIFACT</artifactId>
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
forgepack.ARTIFACT.enabled=true
forgepack.ARTIFACT.property-name=default-value
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

<div style="text-align: center;">

__⭐ Did you like the project? Leave a star! ⭐__

[![GitHub stars](https://img.shields.io/github/stars/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT)
[![GitHub forks](https://img.shields.io/github/forks/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT/fork)
[![GitHub watchers](https://img.shields.io/github/watchers/forgepack/forgepack-ARTIFACT?style=social)](https://github.com/forgepack/forgepack-ARTIFACT)

__Made by [Forgepack](https://github.com/forgepack)__

</div>