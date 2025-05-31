# AI Engineering Course - Kotlin Implementation

Kotlin implementations of the [Scrimba AI Engineering Path](https://scrimba.com/the-ai-engineer-path-c02v) assignments and examples. This project translates JavaScript-based AI engineering concepts into practical Kotlin Spring Boot applications.

**🌐 Live Demo**: [https://ai.rodland.no/](https://ai.rodland.no/) - Interactive demos of AI engineering patterns in Kotlin

## Projects

### 🚀 [Kotlin Spring Boot Application](kotlin/)

A comprehensive Spring Boot web application that implements Scrimba AI Engineering course concepts in Kotlin. Features interactive demos and working examples of all major AI engineering patterns covered in the course.

#### Live Interactive Demos
The Kotlin implementation features comprehensive interactive demos covering all major AI engineering concepts from the Scrimba course:

- **Core AI APIs** - Chat completion, embeddings, and text processing
- **Data & Search** - Vector databases, semantic search, and RAG pipelines  
- **Real-world Integration** - Live financial data and external API connections
- **Advanced Agents** - Multi-turn conversations and reasoning + acting patterns
- **Production Patterns** - Error handling, session management, and scalable architectures

Each demo includes working code examples, interactive web interfaces, and detailed explanations of the underlying concepts. See the [Kotlin project README](kotlin/README.md) for the complete list of available demonstrations.

#### Technology Stack
- **Backend**: Spring Boot 3.5.0 with Kotlin
- **Frontend**: Thymeleaf templates with Bootstrap 5.3.0
- **AI Services**: OpenAI API (chat completion, embeddings)
- **Database**: Supabase (PostgreSQL with vector extensions)
- **Financial Data**: Polygon.io API
- **HTTP Client**: Ktor for async API calls
- **Build**: Maven with Docker containerization

[→ View Kotlin Project Details](kotlin/README.md)

### 🌐 [JavaScript/Node.js Examples](js/)

Original JavaScript implementations and experiments from the Scrimba AI Engineering course, including frontend examples and serverless implementations.

#### Features
- Vanilla JavaScript implementations
- Cloudflare Workers integration
- OpenAI API examples
- Modern web development patterns

[→ View JavaScript Project Details](js/README.md)

## About the Scrimba AI Engineering Course

This repository implements concepts from the **Scrimba AI Engineering Path** - a practical, JavaScript-focused course for web developers eager to use the latest AI technologies. The course covers:

1. **OpenAI API** - Building applications that understand and generate human-like text
2. **HuggingFace** - Open-source ML model hub for sharing and experimenting
3. **Embeddings and Vector Databases** - Infusing apps with specialized knowledge
4. **AI Agents** - Using AI to solve problems and complete tasks
5. **OpenAI's Assistants API** - Seamlessly integrating AI models into applications

## Why Kotlin Implementation?

While the original course uses JavaScript, this project demonstrates that these AI engineering patterns work equally well in other languages. The Kotlin implementation provides:

- **Type Safety** - Compile-time guarantees and better IDE support
- **Spring Boot Integration** - Production-ready web applications with minimal configuration
- **JVM Ecosystem** - Access to mature libraries and enterprise-grade deployment options
- **Learning Tool** - Deeper understanding through reimplementation in a different paradigm

## API Integrations

### OpenAI API
- Chat completion for conversational AI
- Text embeddings for semantic search
- Model parameter customization
- Response streaming and error handling

### Supabase Vector Database
- Vector similarity search
- Document embedding storage
- PostgreSQL with pgvector extension
- Real-time data synchronization

### Polygon.io Financial API
- Real-time and historical stock data
- Market aggregates and technical indicators
- Multi-ticker data retrieval

## Quick Start

1. **Explore Live Demos**: Visit [ai.rodland.no](https://ai.rodland.no/) to try interactive examples
2. **Run Kotlin App Locally**: See [kotlin/README.md](kotlin/README.md) for setup instructions
3. **Check JavaScript Examples**: See [js/README.md](js/README.md) for frontend examples
4. **View Source Code**: Browse repository structure and implementation details

## Links

- 🚀 **Live Application**: [https://ai.rodland.no/](https://ai.rodland.no/)
- 💻 **Source Code**: [GitHub Repository](https://github.com/fmmr/2025_ai_eng)
- 🔄 **CI/CD**: [GitHub Actions](https://github.com/fmmr/2025_ai_eng/actions)
- 📚 **Documentation**: Individual project READMEs in subdirectories