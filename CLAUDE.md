# Instructions for Claude Code

ALWAYS ALWAYS call me BOSS when you address me.

We'll be working in the kotlin directory.
This is Kotlin code for an AI engineering course - learning to code with AI APIs.

## Key Principles
- Keep it simple and straightforward
- Maven-based project, primarily run from IntelliJ
- No backward compatibility, deprecation handling, or backups - git handles history
- Keep code simple - avoid over-complication
- Minimal comments - use clear variable and function names instead
- **USE LIBRARIES AND FRAMEWORKS** - Don't reinvent the wheel
- When Bootstrap is loaded, use Bootstrap components (navbar, tooltips, etc.)
- Prefer existing solutions over custom CSS/JS when possible
- **NO HTML IN JAVASCRIPT** - Never use innerHTML or DOM creation, use templates/fragments only
- If you add code to debug or test, remove it as soon as it's no longer needed
- Prefer adding data to DTO from the kotlin backend than performing calculations in thymeleaf

## Project Structure
- Maven-based Kotlin project with Spring Boot 3.5.0
- Focus on AI API integration and learning
- Uses Bootstrap 5.3.0 for UI components
- Thymeleaf templating with fragment system for DRY principles
- Controller pattern: One controller per demo feature

## Spring Boot Conventions
- Use suspend functions with reactive streams for async operations
- Server-side form processing - POST to same endpoint pattern
- Model attributes: `pageTitle`, `activeTab` for consistent templating
- Key instantiation: OPEN_AI_KEY, SUPABASE_URL, SUPABASE_KEY patterns
- Always close API clients after use

## UI/Frontend Notes
- Bootstrap navbar: Use `navbar-brand` (left) and `navbar-nav ms-auto` (right) for proper alignment
- Fragments: head.html, navigation.html, footer.html, scripts.html for consistent includes
- Tooltips: Use Bootstrap's `data-bs-toggle="tooltip"` instead of custom CSS
- Form pattern: Preserve form data with `formData` model attribute on errors

## Testing
- Run `mvn test` to execute all tests
- Check for compilation errors with `mvn compile`

## Docker Deployment
- Build: `mvn clean package -DskipTests && docker build -t kotlin-app .`
- Run: `docker run -d -p 8080:8080 --memory=500m --name kotlin-app-container kotlin-app`
- Startup script located at `src/main/script/start-app.sh`

## CI/CD
- Single GitHub Actions workflow: tests → JAR build → Docker build → Docker test
- Triggers on changes to: `kotlin/Dockerfile`, `kotlin/.dockerignore`, `kotlin/pom.xml`, `kotlin/src/**`, `.github/workflows/kotlin-ci.yml`
- No redundant builds, efficient caching