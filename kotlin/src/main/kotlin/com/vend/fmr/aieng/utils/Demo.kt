package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.utils.DemoContentType.*

enum class Demo(
    val id: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String,
    val icon: String,
    val emoji: String,
    val status: DemoStatus,
    val category: DemoCategory,
    val route: String? = null,
    val courseContent: Boolean = true,
    val soloProject: Boolean = false,
    val contentType: DemoContentType = if (soloProject) COURSE_CONTENT_SOLO else (if (courseContent) COURSE_CONTENT else PERSONAL_EXPLORATION)
) {


    // Core AI Demos
    PROMPT_ENGINEERING(
        id = "prompt-engineering",
        title = "Prompt Engineering",
        shortDescription = "Master the art of crafting effective prompts",
        longDescription = "Master the art of crafting effective prompts. Compare different prompting techniques to see how small changes dramatically impact AI response quality and accuracy.",
        icon = "bi-pencil-square",
        emoji = "📝",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/prompt-engineering"
    ),

    CHAT_COMPLETION(
        id = "chat-completion",
        title = "Chat Completion",
        shortDescription = "Core OpenAI integration from the Scrimba course",
        longDescription = "Core OpenAI integration from the Scrimba course. Interactive form to generate AI responses with customizable prompts, system messages, and model parameters.",
        icon = "bi-chat-dots",
        emoji = "🤖",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chat-completion"
    ),

    CHAT_INTERACTIVE(
        id = "chat",
        title = "Interactive Chat",
        shortDescription = "Create conversational AI interfaces",
        longDescription = "Create conversational AI interfaces with context awareness, session management, and conversation history.",
        icon = "bi-chat-left-text",
        emoji = "💬",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chat"
    ),

    TEMPERATURE_EFFECTS(
        id = "temperature-effects",
        title = "Temperature Effects",
        shortDescription = "Interactive temperature parameter demo",
        longDescription = "Interactive temperature parameter demo. See how temperature affects AI creativity vs consistency with the same prompt and real-time comparisons.",
        icon = "bi-thermometer-half",
        emoji = "🌡️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/temperature-effects"
    ),

    TOP_P_EFFECTS(
        id = "top-p-effects",
        title = "Top-P Effects",
        shortDescription = "Interactive top-p parameter demo",
        longDescription = "Interactive top-p parameter demo. Discover how top-p affects AI vocabulary selection and word diversity with live response comparisons.",
        icon = "bi-bullseye",
        emoji = "🎯",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/top-p-effects"
    ),

    FINE_TUNING(
        id = "fine-tuning",
        title = "Fine-tuning",
        shortDescription = "Custom model training with OpenAI's fine-tuning API",
        longDescription = "Custom model training with OpenAI's fine-tuning API. Train specialized models on your data for specific tasks and improved performance.",
        icon = "bi-arrow-up-circle",
        emoji = "⚙️",
        status = DemoStatus.NEXT_YEAR,
        category = DemoCategory.CORE_AI
    ),


    EMBEDDINGS(
        id = "embeddings",
        title = "Embeddings",
        shortDescription = "Convert text into high-dimensional vectors",
        longDescription = "Embeddings implementation from the Scrimba course. Convert text into high-dimensional vectors using OpenAI's models, with detailed analysis and visualization of the generated vectors.",
        icon = "bi-diagram-3",
        emoji = "🔢",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/embeddings"
    ),

    CHUNKING(
        id = "chunking",
        title = "Chunking",
        shortDescription = "Text processing fundamentals from the course",
        longDescription = "Text processing fundamentals from the course. Interactive chunking tool with customizable size and overlap settings, essential for preparing data for AI processing.",
        icon = "bi-scissors",
        emoji = "✂️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chunking"
    ),

    POLLYGLOT(
        id = "pollyglot",
        title = "PollyGlot",
        shortDescription = "Multi-language translation bot",
        longDescription = "Multi-language translation bot with conversation context. Translate between dozens of languages while maintaining conversation flow and cultural nuances.",
        icon = "bi-translate",
        emoji = "🌍",
        status = DemoStatus.SOMEDAY,
        category = DemoCategory.CORE_AI,
        soloProject = true
    ),

    // Data Demos
    STOCK_DATA(
        id = "stock",
        title = "Stock Data",
        shortDescription = "Fetch real-time stock data from Polygon.io API",
        longDescription = "Fetch real-time stock data from Polygon.io API and generate AI-powered financial analysis. See the complete pipeline from data fetching through intelligent market insights.",
        icon = "bi-graph-up",
        emoji = "📈",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/stock"
    ),

    VECTOR_DATABASE(
        id = "supabase",
        title = "Vector Database",
        shortDescription = "Vector database concepts from the Scrimba course",
        longDescription = "Vector database concepts from the Scrimba course. Demonstrates semantic search with embeddings and PostgreSQL pgvector extension using Supabase.",
        icon = "bi-database",
        emoji = "🗄️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/supabase"
    ),

    RAG(
        id = "rag",
        title = "RAG",
        shortDescription = "Advanced RAG implementation from the Scrimba course",
        longDescription = "Advanced RAG implementation from the Scrimba course. Complete pipeline: query→embedding→vector search→context-aware AI response using movie/podcast database.",
        icon = "bi-link-45deg",
        emoji = "🔗",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/rag"
    ),

    POPCHOICE(
        id = "popchoice",
        title = "PopChoice",
        shortDescription = "Smart movie recommendation engine",
        longDescription = "Smart movie recommendation engine with AI-powered suggestions. Analyze preferences, mood, and viewing history to find the perfect film for any occasion.",
        icon = "bi-film",
        emoji = "🍿",
        status = DemoStatus.DREAMS,
        category = DemoCategory.DATA,
        soloProject = true
    ),

    // Open Source Models
    HUGGINGFACE_CLASSIFICATION(
        id = "huggingface-classification",
        title = "HuggingFace Classification",
        shortDescription = "Text classification using open-source BART model",
        longDescription = "Text classification using open-source BART model from HuggingFace. Classify text into custom categories with confidence scores and detailed analysis.",
        icon = "bi-tags",
        emoji = "🏷️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-classification"
    ),

    HUGGINGFACE_SUMMARIZATION(
        id = "huggingface-summarization",
        title = "HuggingFace Summarization",
        shortDescription = "Text summarization using open-source BART model",
        longDescription = "Text summarization using open-source BART model from HuggingFace. Compress long texts into concise summaries with customizable length parameters.",
        icon = "bi-file-text",
        emoji = "📄",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-summarization"
    ),

    OBJECT_DETECTION(
        id = "huggingface-object-detection",
        title = "Object Detection",
        shortDescription = "Object detection in images using DETR model",
        longDescription = "Object detection in images using DETR (Detection Transformer) model from HuggingFace. Detect and locate objects with bounding boxes and confidence scores.",
        icon = "bi-search",
        emoji = "🔍",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-object-detection"
    ),

    // Vision & Image
    FILM_FUSION(
        id = "film-fusion",
        title = "Film Fusion",
        shortDescription = "Merge movies and art into stunning AI-generated posters",
        longDescription = "Merge movies and art into stunning AI-generated posters. Combine cinematic imagery with artistic styles and fine-tune with different image generation parameters, models, and quality settings for perfect results.",
        icon = "bi-film",
        emoji = "🎬",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/film-fusion"
    ),

    IMAGE_EDITING(
        id = "image-editing",
        title = "AI Image Editing",
        shortDescription = "AI-powered image editing with DALL-E 2",
        longDescription = "AI-powered image editing with DALL-E 2. Edit existing images by describing what you want to add or change in specific areas using mask-based editing.",
        icon = "bi-brush",
        emoji = "✏️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/image-editing"
    ),

    GPT4_VISION(
        id = "gpt4-vision",
        title = "GPT-4 with Vision",
        shortDescription = "Multi-modal AI capabilities with image analysis",
        longDescription = "Multi-modal AI capabilities with image analysis. Analyze images with GPT-4 Vision using our curated gallery of demo images and suggested prompts.",
        icon = "bi-eye",
        emoji = "👁️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/gpt4-vision"
    ),

    // AI Agents
    REACT_AGENT(
        id = "react",
        title = "ReAct Agent",
        shortDescription = "AI agent implementation from the Scrimba course",
        longDescription = "AI agent implementation from the Scrimba course. Uses ReAct (Reasoning + Acting) pattern to solve problems through iterative thinking and action.",
        icon = "bi-cpu",
        emoji = "🧠",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.AI_AGENTS,
        route = "/demo/react"
    ),

    FUNCTION_CALLING(
        id = "function-calling",
        title = "Function Calling Agent",
        shortDescription = "OpenAI's native function calling agent",
        longDescription = "OpenAI's native function calling agent. Uses structured function schemas to let AI directly call external tools, compare with ReAct's manual approach.",
        icon = "bi-gear",
        emoji = "🛠️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.AI_AGENTS,
        route = "/demo/function-calling"
    ),
    TRAVEL_AGENT(
        id = "travel-agent",
        title = "Travel Agent",
        shortDescription = "Practical AI agent for trip planning",
        longDescription = "Practical AI agent for trip planning. Book flights, find accommodations, create itineraries using multiple APIs and AI reasoning for seamless travel experiences.",
        icon = "bi-airplane",
        emoji = "✈️",
        status = DemoStatus.TIME,
        category = DemoCategory.AI_AGENTS,
        soloProject = true

    ),
    ASSISTANTS_API(
        id = "assistants-api",
        title = "Assistants API",
        shortDescription = "OpenAI's Assistants API integration",
        longDescription = "OpenAI's Assistants API integration from the Scrimba course. Build persistent AI assistants with file handling, code execution, and long-term memory.",
        icon = "bi-robot",
        emoji = "🎯",
        status = DemoStatus.COFFEE,
        category = DemoCategory.AI_AGENTS
    ),

    // MCP Protocol (Additional Explorations)
    MCP_SERVER(
        id = "mcp-server",
        title = "MCP Server",
        shortDescription = "Build a custom MCP server using Spring AI",
        longDescription = "Build a custom MCP server using Spring AI. Expose tools and resources that Claude Desktop and other MCP clients can discover and use automatically. Perfect for creating your own AI tool ecosystem.",
        icon = "bi-server",
        emoji = "🛠️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.MCP_PROTOCOL,
        route = "/demo/mcp-server",
        courseContent = false,
    ),

    MCP_PROTOCOL_DEMO(
        id = "mcp-protocol",
        title = "MCP Protocol Demo",
        shortDescription = "Learn the Model Context Protocol fundamentals",
        longDescription = "Learn the Model Context Protocol (MCP) fundamentals. Educational demo showing JSON-RPC 2.0 calls, tool discovery, and parameter schemas step-by-step.",
        icon = "bi-diagram-2",
        emoji = "🔗",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.MCP_PROTOCOL,
        route = "/demo/mcp-protocol",
        courseContent = false,
        contentType = HACKDAY
    ),

    MCP_ASSISTANT(
        id = "mcp-assistant",
        title = "MCP Assistant",
        shortDescription = "AI-powered assistant that dynamically discovers tools",
        longDescription = "AI-powered assistant that dynamically discovers and uses external tools via MCP. Chat interface with OpenAI function calling and session memory. See real-world AI tool integration in action.",
        icon = "bi-wrench",
        emoji = "🔧",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.MCP_PROTOCOL,
        route = "/demo/mcp-assistant",
        courseContent = false,
        contentType = HACKDAY
    ),

    // Frameworks (Additional Explorations)
    LANGCHAIN4J(
        id = "langchain4j",
        title = "LangChain4j Framework",
        shortDescription = "Interactive demo showcasing LangChain4j's declarative AI services",
        longDescription = "Interactive demo showcasing LangChain4j's declarative AI services. Test text analysis, summarization, and sentiment classification using annotated interfaces vs. our custom implementations.",
        icon = "bi-link",
        emoji = "🦜",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.FRAMEWORKS,
        route = "/demo/langchain4j",
        courseContent = false
    ),

    SPRING_AI(
        id = "spring-ai",
        title = "Spring AI Framework",
        shortDescription = "Official Spring AI 1.0.0 framework demo",
        longDescription = "Official Spring AI 1.0.0 framework demo. Compare Spring's native AI integration with auto-configuration, dependency injection, and Spring-native patterns vs. LangChain4j and custom implementations.",
        icon = "bi-leaf",
        emoji = "🍃",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.FRAMEWORKS,
        route = "/demo/spring-ai",
        courseContent = false
    ),


    // Additional Explorations (Future)
    MCP_REGISTRY(
        id = "mcp-registry",
        title = "MCP Registry",
        shortDescription = "Custom MCP server registry and discovery service",
        longDescription = "Custom MCP server registry and discovery service. Browse, connect, and manage multiple MCP servers with a unified interface.",
        icon = "bi-globe",
        emoji = "\uD83C\uDF0D",
        status = DemoStatus.WILD_IDEAS,
        category = DemoCategory.MCP_PROTOCOL,
        courseContent = false
    ),

    OPENAI_JAVA(
        id = "openai-java",
        title = "OpenAI Java Library",
        shortDescription = "Explore OpenAI's official Java/Kotlin library",
        longDescription = "Explore OpenAI's official Java/Kotlin library with type-safe builders, streaming support, and structured outputs. Compare with our custom HTTP implementation.",
        icon = "bi-cup-hot",
        emoji = "☕",
        status = DemoStatus.PROCRASTINATING,
        category = DemoCategory.FRAMEWORKS,
        courseContent = false
    );

    companion object {
        fun getByCategory(category: DemoCategory): List<Demo> {
            return entries.filter { it.category == category }
        }

        fun getByNavbarGroup(navbarGroup: NavbarGroup): List<Demo> {
            return entries.filter { it.category.navbarGroup == navbarGroup }
        }
    }
}