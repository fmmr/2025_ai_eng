# AI Engineering Course - Kotlin Implementation

Kotlin implementation of the [Scrimba AI Engineering Path](https://scrimba.com/the-ai-engineer-path-c02v) assignments and examples. This Spring Boot web application translates JavaScript-based AI engineering concepts into production-ready Kotlin code with interactive demos.

**🌐 Live Demo**: [https://ai.rodland.no/](https://ai.rodland.no/) (deployed via Northflank)

## Overview

This project implements the core concepts from the Scrimba AI Engineering course in Kotlin. Rather than following along with JavaScript examples, this provides equivalent functionality using Spring Boot, demonstrating that AI engineering patterns are language-agnostic and work excellently in the JVM ecosystem.

## Web Application

### Interactive Demos

<!-- DEMO_LIST_START -->
## 🧠 Core AI

- **📝 Prompt Lab** - Crafting effective prompts
- **🤖 Chat Basics** - Core OpenAI integration
- **💬 Chat Studio** - Conversational AI interface
- **🌡️ Temperature Play** - AI creativity vs consistency
- **🎯 Top-P Magic** - Vocabulary selection control
- **⚙️ Fine-Tune Factory** - Custom model training (Maybe Next Year)
- **🔢 Vector Space** - Text to vector conversion
- **✂️ Text Slicer** - Text segmentation tool
- **🌍 PollyGlot** - Contextual translation bot (Solo Project - Someday)

## 🗄️ Data

- **📈 Stock Oracle** - AI-powered financial analysis
- **🗄️ Vector Vault** - Semantic search with embeddings
- **🔗 RAG Engine** - Retrieval-Augmented Generation
- **🍿 PopChoice** - AI movie recommendations (Solo Project - Dreams)

## 🚀 Open Source

- **🏷️ Text Classifier** - Open-source text classification
- **📄 Text Squisher** - Open-source text summarization
- **🔍 Object Spotter** - Visual object recognition

## 👁️ Vision & Image

- **🎬 Poster Lab** - AI-generated movie posters
- **✏️ Image Remix** - Mask-based image editing
- **👁️ Vision Quest** - Multi-modal image analysis

## 🎯 AI Agents

- **🧠 ReAct Playground** - Reasoning + Acting pattern
- **🛠️ Tool Master** - Native OpenAI tool integration
- **✈️ Trip Wizard** - AI-powered trip planning (Solo Project - When I Have Time)
- **🎯 AI Sidekick** - Persistent AI assistants (After Coffee)

## 🔗 MCP Protocol

- **🛠️ MCP Toolbox** - Custom AI tool server (Personal Exploration)
- **🔗 MCP Decoded** - Protocol fundamentals (Hackday)
- **🔧 MCP Wizard** - Dynamic tool discovery (Hackday)
- **🌍 MCP Catalog** - Server discovery service (Personal Exploration - Wild Ideas)

## 🏗️ Frameworks

- **🦜 LangChain Lab** - Declarative AI services (Personal Exploration)
- **🍃 Spring AI** - Native Spring integration (Personal Exploration)
- **☕ OpenAI Toolkit** - Official Java/Kotlin SDK (Personal Exploration - Procrastinating)


<!-- DEMO_LIST_END -->

### Technology Stack
- **Backend**: Spring Boot 3.5.0 with Kotlin
- **Frontend**: Thymeleaf templates with Bootstrap 5.3.0
- **AI Services**: OpenAI API (chat completion, embeddings), HuggingFace Inference API (classification, summarization), LangChain4j Framework
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

### HuggingFace Inference API
- Text classification with BART models
- Text summarization with customizable parameters
- Open-source model integration
- Free tier access to community models

### LangChain4j Framework
- Declarative AI service interfaces with annotations
- Type-safe prompt templating and variable injection
- Multi-provider support (OpenAI, Anthropic, HuggingFace, local models)
- Spring Boot auto-configuration and dependency injection
- Enterprise-ready patterns for production AI applications

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

# HuggingFace Integration
HF_TOKEN=your_huggingface_token_here
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
