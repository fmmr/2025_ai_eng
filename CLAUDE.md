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
- Don't guess - find out the answer by inspecting and looking at documentation

## Stack & Standards
- Spring Boot 3.5.0 with Kotlin, Bootstrap 5.3.0 + Thymeleaf fragments
- One controller per demo feature, suspend functions for async operations
- Model attributes: `pageTitle`, `activeTab`
- API keys: OPEN_AI_KEY, SUPABASE_URL, SUPABASE_KEY patterns
- Bootstrap components over custom CSS/JS
- Use Bootstrap icons by default (bi-*), and fallback to locally stored icons from https://simpleicons.org/
- Data to JS via `th:attr="data-key=${value}"` → `element.dataset.key`
- Code examples: `<pre><code class="language-kotlin">` in demos
- Form errors: preserve with `formData` model attribute

## Development Workflow
1. **Command Line First** - Build one-liner from Main.kt calling `*Examples.kt` functions
2. **Web Demo Second** - After command line works, create Spring Boot controller using same impl classes

## API Implementation
- impl classes accept `debug: Boolean = false` parameter (silent by default)
- Use kotlinx.serialization data classes - **NO manually constructed JSON**
- Create `DataClasses.kt` in each impl package with `@Serializable` annotations
- Main.kt contains single line calling demo function
- Demo logic in `*Examples.kt` files with all printlns and presentation

## Build & Deploy
- Test: `mvn test`
- Build: `mvn clean package -DskipTests && docker build -t kotlin-app .`
- Run: `docker run -d -p 8080:8080 --memory=500m --name kotlin-app-container kotlin-app`
- CI/CD: tests → JAR → Docker → test

## Other Directories
- `js/` - DodgyDave (Vite/JavaScript stock tips chatbot) - minimal focus
- `polyglot/` - PolyGlot (Node.js/Socket.io translation chat) - minimal focus
- Both have separate package.json and deployment pipelines

## Demo Updates
When adding demos, update: home demo-box, navbar, README - keep order consistent.