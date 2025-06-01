# AI Engineering Course - Kotlin Implementation

Kotlin implementation of the [Scrimba AI Engineering Path](https://scrimba.com/the-ai-engineer-path-c02v) assignments and examples. This Spring Boot web application translates JavaScript-based AI engineering concepts into production-ready Kotlin code with interactive demos.

**🌐 Live Demo**: [https://ai.rodland.no/](https://ai.rodland.no/) (deployed via Northflank)

## Overview

This project implements the core concepts from the Scrimba AI Engineering course in Kotlin. Rather than following along with JavaScript examples, this provides equivalent functionality using Spring Boot, demonstrating that AI engineering patterns are language-agnostic and work excellently in the JVM ecosystem.

## Web Application

### Interactive Demos

**✅ Completed:**
- **🤖 Chat Completion** - OpenAI chat completion with customizable prompts and parameters
- **📈 Stock Data** - Real-time financial data from Polygon.io API with AI analysis
- **💬 Chat** - Multi-turn conversational interfaces with context awareness and session management
- **🔢 Embeddings** - Text-to-vector conversion with statistical analysis and visualization  
- **✂️ Chunking** - Interactive text splitting with overlap visualization
- **🗄️ Vector Database** - Supabase vector operations and semantic search
- **🔗 RAG** - Full Retrieval-Augmented Generation pipeline showing query→embedding→search→response
- **🧠 ReAct Agent** - Reasoning + Acting pattern with step-by-step problem solving and function calling
- **🛠️ Function Calling Agent** - OpenAI's native function calling with structured tool schemas

**🔜 Coming Soon:**
- **📝 Prompt Engineering** - Master crafting effective prompts with quality comparison techniques
- **🎛️ Chat Parameters** - Interactive playground for temperature, top-p, and other OpenAI parameters
- **🎯 Fine-tuning** - Custom model training with OpenAI's fine-tuning API
- **🌍 PollyGlot** - Multi-language translation bot (Solo Project)
- **🤗 HuggingFace** - Open-source ML model hub integration
- **🍿 PopChoice** - Smart movie recommendation engine (Solo Project)
- **✈️ Travel Agent** - Practical AI agent for trip planning (Solo Project)
- **🎨 Image Generation** - DALL-E integration for AI-powered image creation
- **🎛️ Tuning Image Generation** - Advanced DALL-E parameter tuning and optimization
- **✏️ Editing Images** - AI-powered image editing and manipulation capabilities
- **👁️ GPT-4 with Vision** - Multi-modal AI capabilities with image analysis and insights
- **🎯 Assistants API** - OpenAI's Assistants API integration
- **☕ OpenAI Java Library** - Explore OpenAI's official Java/Kotlin library vs custom implementation

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