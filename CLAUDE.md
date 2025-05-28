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

## Project Structure
- Maven-based Kotlin project with Spring Boot
- Focus on AI API integration and learning
- Uses Bootstrap 5.3.0 for UI components
- Thymeleaf templating with fragment system for DRY principles

## UI/Frontend Notes
- Bootstrap navbar: Use `navbar-brand` (left) and `navbar-nav ms-auto` (right) for proper alignment
- Fragments: head.html, navigation.html, footer.html, scripts.html for consistent includes
- Tooltips: Use Bootstrap's `data-bs-toggle="tooltip"` instead of custom CSS