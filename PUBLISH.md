# _Publish on Maven Central_

## Summary
- [1. Publishing to Maven Central](#1-publishing-to-maven-central)
- [2. Prove Namespace Ownership](#2-prove-namespace-ownership)
- [3. Create a GPG Key](#3-create-a-gpg-key)
- [4. Publish the GPG Key](#4-publish-the-gpg-key)
- [5. Configure Maven Credentials](#5-configure-maven-credentials)
- [6. Publish the Artifact](#6-publish-the-artifact)
- [7. Tag the Release](#7-tag-the-release)

Artifact published on Maven Central.
```bash
dev.forgepack:ARTIFACT:{VERSION}
```

## 1. PUBLISHING TO MAVEN CENTRAL

This document describes the steps required to publish a library to Maven Central using the `dev.forgepack` namespace.

### 1.1. Create Sonatype Account

Create an account on Sonatype Central Portal.

After creating the account:
1. Claim your __groupId / namespace__
2. Prove ownership of the namespace

Example namespace:
```
dev.forgepack
```

## 2. PROVE NAMESPACE OWNERSHIP

Sonatype requires proof that you control the namespace.

Common methods:
### 2.1. Option A — DNS verification

Create a __TXT DNS record__ in the domain associated with your namespace.

Example:
```bash
Type: TXT
Name: dev.forgepack
Value: <verification-key>
```

### 2.2. Option B — GitHub namespace

You can also verify ownership using a GitHub repository under the same namespace.

## 3. CREATE A GPG KEY

Maven Central requires all artifacts to be __cryptographically signed__.

Generate or list your GPG key:
```bash
gpg --list-secret-keys --keyid-format LONG
```

Example output:

```bash
sec   rsa4096/ABCD1234EF567890 2026-03-05 [SC]
      9A1B2C3D4E5F67890123456789ABCDEF12345678
uid           Marcelo Gadelha <gadelha.ti@forgepack.dev>
```

The __fingerprint__ is the long key:

```
9A1B2C3D4E5F67890123456789ABCDEF12345678
```

## 4. PUBLISH THE GPG KEY

Upload the public key to a public keyserver.
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys SEU_FINGERPRINT
```

Example:
```bash
gpg --keyserver keyserver.ubuntu.com --send-keys 9A1B2C3D4E5F67890123456789ABCDEF12345678
```

Verify that the key is publicly available:
```bash
gpg --keyserver keyserver.ubuntu.com --recv-keys SEU_FINGERPRINT
```

## 5. Configure Maven Credentials

Edit your Maven settings file:
```bash
~/.m2/settings.xml
```

Add your Sonatype credentials:
```xml
<servers>
    <server>
        <id>central</id>
        <username>SEU_USUARIO</username>
        <password>SEU_TOKEN</password>
    </server>
</servers>
```

## 6. Publish the Artifact

Run the Maven deploy command:
```bash
mvn clean deploy
```

This will publish the following artifacts:
```bash
ARTIFACT-{VERSION}.jar
ARTIFACT-{VERSION}.pom
ARTIFACT-{VERSION}-sources.jar
ARTIFACT-{VERSION}-javadoc.jar
ARTIFACT-{VERSION}.asc
```

## 7. Tag the Release

After publishing, create a Git tag for the version.
```bash
git tag v{VERSION}
git push origin v{VERSION}
```

## Versioning Strategy

This project follows __Semantic Versioning__.
```properties
MAJOR.MINOR.PATCH
```

Examples:
```txt
1.0.0  → first stable release
1.1.0  → new feature
1.1.1  → bug fix
2.0.0  → breaking change
```

| Change          | Version Type |
| --------------- | ------------ |
| Bug fix         | PATCH        |
| New feature     | MINOR        |
| Breaking change | MAJOR        |
