# AI Engineering Course - Kotlin Implementation

Kotlin implementation of the [Scrimba AI Engineering Path](https://scrimba.com/the-ai-engineer-path-c02v) assignments and examples. This Spring Boot web application translates JavaScript-based AI engineering concepts into production-ready Kotlin code with interactive demos.

**🌐 Live Demo**: [https://ai.rodland.no/](https://ai.rodland.no/) (deployed via Northflank)

## Overview

This project implements the core concepts from the Scrimba AI Engineering course in Kotlin. Rather than following along with JavaScript examples, this provides equivalent functionality using Spring Boot, demonstrating that AI engineering patterns are language-agnostic and work excellently in the JVM ecosystem.

## Web Application

### Interactive Demos

<!-- DEMO_LIST_START -->
[INFO] Scanning for projects...
[INFO] 
[INFO] -------------------------< com.vend.fmr:aieng >-------------------------
[INFO] Building aieng 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- git-commit-id:8.0.2:revision (get-the-git-infos) @ aieng ---
[INFO] dotGitDirectory '/home/runner/work/2025_ai_eng/2025_ai_eng/.git'
[INFO] Using environment variable based branch name. GITHUB_REF = refs/heads/main (branch = main)
[INFO] Collected git.branch with value main
[INFO] Collected git.commit.id.abbrev with value 1bc845d
[INFO] Collected git.commit.time with value 2025-06-06T09:56:06Z
[INFO] including property 'git.commit.id.abbrev' in results
[INFO] including property 'git.branch' in results
[INFO] including property 'git.commit.time' in results
[INFO] 
[INFO] --- git-commit-id:8.0.2:revision (default) @ aieng ---
[INFO] dotGitDirectory '/home/runner/work/2025_ai_eng/2025_ai_eng/.git'
[INFO] Using environment variable based branch name. GITHUB_REF = refs/heads/main (branch = main)
[INFO] Collected git.branch with value main
[INFO] Collected git.commit.id.abbrev with value 1bc845d
[INFO] Collected git.commit.time with value 2025-06-06T09:56:06Z
[INFO] including property 'git.commit.id.abbrev' in results
[INFO] including property 'git.branch' in results
[INFO] including property 'git.commit.time' in results
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ aieng ---
[INFO] Copying 52 resources from src/main/resources to target/classes
[INFO] 
[INFO] --- compiler:3.14.0:compile (default-compile) @ aieng ---
[INFO] Nothing to compile - all classes are up to date.
[INFO] 
[INFO] --- kotlin:2.1.21:compile (compile) @ aieng ---
[INFO] Applied plugin: 'kotlinx-serialization'
[INFO] Applied plugin: 'spring'
[INFO] 
[INFO] --- exec:3.1.0:java (default-cli) @ aieng ---
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.pom
Progress (1): 1.4/8.2 kBProgress (1): 2.8/8.2 kBProgress (1): 4.1/8.2 kBProgress (1): 5.5/8.2 kBProgress (1): 6.9/8.2 kBProgress (1): 8.2 kB                        Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.pom (8.2 kB at 27 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.pom
Progress (1): 1.4/11 kBProgress (1): 2.8/11 kBProgress (1): 4.1/11 kBProgress (1): 5.5/11 kBProgress (1): 6.9/11 kBProgress (1): 8.3/11 kBProgress (1): 9.7/11 kBProgress (1): 11 kB                       Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.pom (11 kB at 916 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/35/commons-parent-35.pom
Progress (1): 16/58 kBProgress (1): 33/58 kBProgress (1): 49/58 kBProgress (1): 58 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/35/commons-parent-35.pom (58 kB at 1.5 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/15/apache-15.pom
Progress (1): 15 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/15/apache-15.pom (15 kB at 2.5 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.jar
Progress (1): 16/267 kBProgress (1): 33/267 kBProgress (1): 49/267 kBProgress (1): 66/267 kBProgress (1): 82/267 kBProgress (1): 98/267 kBProgress (1): 115/267 kBProgress (1): 131/267 kBProgress (1): 147/267 kBProgress (1): 164/267 kBProgress (1): 180/267 kBProgress (1): 197/267 kBProgress (1): 213/267 kBProgress (1): 229/267 kBProgress (1): 246/267 kBProgress (1): 256/267 kBProgress (1): 267 kB                        Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.jar (267 kB at 6.7 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-component-annotations/2.1.1/plexus-component-annotations-2.1.1.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.jar
Progress (1): 4.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-component-annotations/2.1.1/plexus-component-annotations-2.1.1.jar (4.1 kB at 684 kB/s)
Progress (1): 1.4/54 kBProgress (1): 2.8/54 kBProgress (1): 4.1/54 kBProgress (1): 5.5/54 kBProgress (1): 6.9/54 kBProgress (1): 8.3/54 kBProgress (1): 9.7/54 kBProgress (1): 11/54 kB Progress (1): 12/54 kBProgress (1): 14/54 kBProgress (1): 15/54 kBProgress (1): 17/54 kBProgress (1): 18/54 kBProgress (1): 19/54 kBProgress (1): 21/54 kBProgress (1): 22/54 kBProgress (1): 23/54 kBProgress (1): 25/54 kBProgress (1): 26/54 kBProgress (1): 28/54 kBProgress (1): 29/54 kBProgress (1): 30/54 kBProgress (1): 32/54 kBProgress (1): 33/54 kBProgress (1): 34/54 kBProgress (1): 36/54 kBProgress (1): 37/54 kBProgress (1): 39/54 kBProgress (1): 40/54 kBProgress (1): 41/54 kBProgress (1): 43/54 kBProgress (1): 44/54 kBProgress (1): 46/54 kBProgress (1): 47/54 kBProgress (1): 48/54 kBProgress (1): 50/54 kBProgress (1): 51/54 kBProgress (1): 52/54 kBProgress (1): 54/54 kBProgress (1): 54 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-exec/1.3/commons-exec-1.3.jar (54 kB at 2.3 MB/s)
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


[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  22.132 s
[INFO] Finished at: 2025-06-06T09:56:48Z
[INFO] ------------------------------------------------------------------------
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
