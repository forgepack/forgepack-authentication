# _Security Overview_

Security practices and considerations for this library.

## 1. DEPENDENCY SECURITY

All dependencies are managed via Maven and should be kept up to date.

```bash
# check for known CVE vulnerabilities in dependencies
mvn org.owasp:dependency-check-maven:check

# check for available dependency updates
mvn versions:display-dependency-updates

# check for available plugin updates
mvn versions:display-plugin-updates
```

## 2. SAFE API DESIGN

- All public API classes in `api/` are designed to be **non-breaking** across minor versions.
- Internal implementation in `internal/` may change and should **never be referenced directly** by consumers.
- No sensitive data (credentials, secrets, tokens) should be logged or stored through library components.
- All user-facing configuration properties must be validated with `@Validated` on the `@ConfigurationProperties` class, obtained by forgepack-core.

## 3. CONFIGURATION PROPERTIES SECURITY

- Sensitive configuration values (passwords, tokens, keys) must be clearly documented with their security implications.
- Recommend consumers use environment variables or a secrets manager instead of hardcoding values in `application.properties`.
- Never set insecure defaults for security-sensitive properties.

## 4. REPORTING VULNERABILITIES

If you discover a security vulnerability in this library, please report it responsibly:

1. **Do not** open a public GitHub issue.
2. Contact the maintainer directly at [https://github.com/gadelhati](https://github.com/gadelhati).
3. Provide a clear description of the vulnerability and steps to reproduce.

We are committed to acknowledging and resolving security issues promptly.