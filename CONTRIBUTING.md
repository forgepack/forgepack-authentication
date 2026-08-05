# CONTRIBUTION GUIDE

Thank you for your interest in contributing to this project!

To ensure code quality and consistency, please follow the guidelines below.

## 1. WORKFLOW

We use a __Simplified Gitflow__ workflow:

1. _Fork_ the repository.  
2. Create a new _branch_ from `main` for your feature or fix:  
   ```bash
   git checkout -b feat/feature-name
   ```
3. Implement your changes.  
4. Make sure all _tests_ pass and _code coverage_ has not decreased (`mvn test`)  
5. Follow the _Commit Convention_ [see below](#7-commit-conventions).
   - __Commit__ your changes (`git commit -m 'feat(amazing): add amazing feature'`)
   - __Push__ to the develop branch (`git push -u origin feature/feature-name`)
6. Open a _Pull Request (PR)_ to the `main` branch.  

## 2. CODE STANDARDS

* __Java:__ Follow the official Spring Boot coding standards and Java naming conventions.  
* __Tests:__ Every new feature or bug fix must include relevant unit and/or integration tests.  
* __Documentation:__ Update the `README.md` and Javadoc if the public API changes.  

## 3. COMMIT CONVENTION

We follow the __Conventional Commits__ specification.  
The format should be:

```bash
<type>(<scope>): <description>
```

__Examples:__

* `feat(city): add state filter to city search`
* `fix(auth): fix authentication failure when token expires`
* `test(service): increase test coverage for ServiceCity`

## 4. CODE REVIEW

Your Pull Request will be reviewed by one of the maintainers.  
Be prepared to receive feedback and make necessary adjustments.

## 5. CODE STANDARDS

- __Test Coverage__: Maintain or improve current coverage
- __Architecture__: Follow the `api/` (public) and `internal/` (private) package separation
- __Clean Code__: Follow SOLID and DRY principles
- __Javadoc__: Document all public types and methods in the `api/` package

## 6. SUGGESTING FEATURES

For feature suggestions:
- __Use Case__: Describe the use case
- __Business Value__: What value does it add to the project
- __Acceptance Criteria__: specific conditions to be met

## 7. COMMIT CONVENTIONS

This project adopts standardized commit conventions:

| Tipo       |                   Description                    | Exemplo                                                       |
|:-----------|:------------------------------------------------:|:--------------------------------------------------------------|
| `feat`     |        A new feature for the application         | `feat(geo): adds a search endpoint using coordinates.`        |
| `fix`      |                    Bug fixes                     | `fix(auth): fixes authentication error during login.`         |
| `docs`     |          Changes to documentation files          | `docs(readme): update the deployment section in the README.`  |
| `style`    |          Styling and formatting changes          | `style(city): adjust indentation in ControllerCity.`          |
| `refactor` | Code refactoring without changing functionality. | `refactor(generic): optimize ServiceGeneric to use Optional.` |
| `perf`     |           Performance-related changes            | `perf(layer): optimizes SQL query in the persistence layer.`  |
| `test`     |           Creating or modifying tests            | `test(city): adds unit testing to ServiceCity.`               |
| `chore`    |        Changes to config, build, CI files        | `chore(configuration): updates Spring Boot version to 3.5.4`  |

### 7.1. Useful Git Commands

```bash
# create a new branch and switch to this new branch
git checkout -b feature/new-feature
# remove a local branch
git branch -d feature/feature-removed
# removes a branch from the remote repository
git push --delete origin feature/feature-removed

# download the updates, but don't change your branch
git fetch remote-branch
# download the updates, and rewrite history
git rebase remote-branch/main
# download the updates, end edit from the penultimate commit
git rebase -i HEAD~3
# push your local branch to the remote
git push origin feature/your-feature

# create a tag
git tag -a v1.4.0 -m "Release version 1.4.0"
# send tag to remote
git push origin v1.4.0

# displays the commit history
git log --oneline --graph --decorate
# displays the repository status
git status --short
# differences between staging and the last commit
git diff --staged
```
