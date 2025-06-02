# Instructions for Claude Code

ALWAYS call me BOSS.

Working in kotlin directory - AI engineering course using Kotlin/Spring Boot.

## Core Principles
- Keep it simple - no over-complication
- Maven-based project, primarily run from IntelliJ  
- Git handles history - no backward compatibility concerns
- **NO USELESS COMMENTS** - Only comments that add real value beyond what code shows
- **USE LIBRARIES AND FRAMEWORKS** - Don't reinvent
- **NO HTML IN JAVASCRIPT** - Use templates/fragments only
- Remove debug code when done
- Process data in Kotlin backend, not Thymeleaf

## Stack
- Spring Boot 3.5.0 with Kotlin
- Bootstrap 5.3.0 + Thymeleaf fragments
- One controller per demo feature
- Suspend functions for async operations
- Server-side form processing (POST to same endpoint)

## Standards
- Model attributes: `pageTitle`, `activeTab`
- API keys: OPEN_AI_KEY, SUPABASE_URL, SUPABASE_KEY patterns
- Bootstrap components over custom CSS/JS
- Data to JS via `th:attr="data-key=${value}"` → `element.dataset.key`
- Code examples: `<pre><code class="language-kotlin">` in demos
- Form errors: preserve with `formData` model attribute

## Build & Deploy
- Test: `mvn test`
- Build: `mvn clean package -DskipTests && docker build -t kotlin-app .`
- Run: `docker run -d -p 8080:8080 --memory=500m --name kotlin-app-container kotlin-app`
- CI/CD: tests → JAR → Docker → test

## Demo Updates
When adding demos, update: home demo-box, navbar, README - keep order consistent.