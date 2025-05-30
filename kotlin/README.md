# AI Engineering Course - Kotlin

A comprehensive Spring Boot web application for learning AI API integration with real-world examples and interactive demos.

**🌐 Live Demo**: [https://ai.rodland.no/](https://ai.rodland.no/) (deployed via Northflank)

## Overview

This project demonstrates practical AI engineering patterns using Kotlin and Spring Boot. It includes interactive web demos for various AI services and APIs, providing hands-on experience with modern AI development workflows.

## Web Application

### Interactive Demos
- **🤖 Chat Completion** - OpenAI chat completion with customizable prompts and parameters
- **🔢 Embeddings** - Text-to-vector conversion with statistical analysis and visualization  
- **✂️ Chunking** - Interactive text splitting with overlap visualization
- **🔗 RAG** - Full Retrieval-Augmented Generation pipeline showing query→embedding→search→response
- **📈 Stock Data** - Real-time financial data from Polygon.io API with AI analysis
- **💬 Chat** - Multi-turn conversational interfaces *(in progress)*
- **🌐 Translator** - AI-powered translation service *(coming soon)*
- **🗄️ Vector Database** - Supabase vector operations and semantic search

### Technology Stack
- **Backend**: Spring Boot 3.5.0 with Kotlin
- **Frontend**: Thymeleaf templates with Bootstrap 5.3.0
- **AI Services**: OpenAI API (chat completion, embeddings)
- **Database**: Supabase (PostgreSQL with vector extensions)
- **Financial Data**: Polygon.io API
- **HTTP Client**: Ktor for async API calls
- **Build**: Maven with Docker containerization

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

## Environment Variables

The application reads configuration from environment variables (with optional `.env` file fallback):

```bash
# OpenAI Integration
OPENAI_API_KEY=your_openai_api_key_here

# Supabase Database
SUPABASE_URL=your_supabase_project_url_here
SUPABASE_ANON_KEY=your_supabase_anon_key_here

# Financial Data (Optional)
POLYGON_API_KEY=your_polygon_api_key_here
```

**Note**: .env file values take precedence over environment variables. The `.env` file is excluded from git for security.

## Development

### Running Locally
```bash
mvn spring-boot:run
```

### Docker Deployment
```bash
mvn clean package -DskipTests
docker build -t kotlin-app .
docker run -p 8080:8080 --memory=500m kotlin-app
```

### Testing
```bash
mvn test
```

The application includes automated CI/CD with GitHub Actions, building and deploying to production on every push.