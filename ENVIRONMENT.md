# __Environment Configuration__

## 1. PREREQUISITES

| Tool              | Minimum version |                                                          Download | Details                            |
|:------------------|:---------------:|------------------------------------------------------------------:|:-----------------------------------|
| __Java JDK__      |       25        |              [OpenJDK 25](https://adoptium.net/temurin/releases/) | Project's main runtime             |
| __Maven__         |      3.8+       |             [Apache Maven](https://maven.apache.org/download.cgi) | Build tool and dependency manager  |
| __IntelliJ IDEA__ |     2024.1+     |             [JetBrains](https://www.jetbrains.com/idea/download/) | Recommended IDE                    |

## 2. SETUP

```bash
# clone the repository
git clone https://github.com/forgepack/forgepack-ARTIFACT
cd forgepack-ARTIFACT

# add remote upstream
git remote add upstream https://github.com/forgepack/forgepack-ARTIFACT

# install and compile
mvn clean install

# run tests
mvn test

# validate project integrity
mvn validate

# generate JaCoCo coverage report
mvn clean test jacoco:report
```

## 3. DEVELOPMENT COMMANDS

```bash
# compile sources only
mvn compile

# run unit tests
mvn test

# package the JAR without running tests
mvn package -DskipTests

# install into local Maven repository (~/.m2)
mvn clean install

# generate Javadoc
mvn javadoc:javadoc

# generate sources JAR
mvn source:jar

# clean, test and package (full lifecycle)
mvn clean package
```

## 4. PUBLISHING

```bash
# full publish to Maven Central (requires GPG key and credentials)
mvn clean deploy
```

> See [PUBLISH.md](PUBLISH.md) for the complete release process.
