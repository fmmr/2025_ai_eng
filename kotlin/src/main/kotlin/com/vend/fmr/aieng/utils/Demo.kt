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
        title = "Prompt Lab",
        shortDescription = "Crafting effective prompts",
        longDescription = "Compare different prompting techniques to see how small changes dramatically impact AI response quality and accuracy. Essential foundation for all AI interactions.",
        icon = "bi-pencil-square",
        emoji = "📝",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/prompt-engineering"
    ),

    CHAT_COMPLETION(
        id = "chat-completion",
        title = "Chat Basics",
        shortDescription = "Core OpenAI integration",
        longDescription = "Interactive form to generate AI responses with customizable prompts, system messages, and model parameters.<br>Foundation demo from the Scrimba course.",
        icon = "/images/openai.svg",
        emoji = "🤖",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chat-completion"
    ),

    CHAT_INTERACTIVE(
        id = "chat",
        title = "Chat Studio",
        shortDescription = "Conversational AI interface",
        longDescription = "Full conversation experience with context awareness, session management, and persistent history.  Build ChatGPT-like interactions with memory.",
        icon = "bi-chat-left-text",
        emoji = "💬",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chat"
    ),

    TEMPERATURE_EFFECTS(
        id = "temperature-effects",
        title = "Temperature Play",
        shortDescription = "AI creativity vs consistency",
        longDescription = "Discover how temperature affects AI output randomness. Compare creative vs consistent responses with the same prompt using different temperature values.",
        icon = "bi-thermometer-half",
        emoji = "🌡️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/temperature-effects"
    ),

    TOP_P_EFFECTS(
        id = "top-p-effects",
        title = "Top-P Magic",
        shortDescription = "Vocabulary selection control",
        longDescription = "Explore how top-p affects AI vocabulary selection and word diversity.<br>See how nucleus sampling shapes response quality and variety in real-time.",
        icon = "bi-bullseye",
        emoji = "🎯",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/top-p-effects"
    ),

    FINE_TUNING(
        id = "fine-tuning",
        title = "Fine-Tune Factory",
        shortDescription = "Custom model training",
        longDescription = "Train specialized models on your data using OpenAI's fine-tuning API.<br>Create domain-specific AI models for improved task performance.",
        icon = "bi-arrow-up-circle",
        emoji = "⚙️",
        status = DemoStatus.NEXT_YEAR,
        category = DemoCategory.CORE_AI
    ),


    EMBEDDINGS(
        id = "embeddings",
        title = "Vector Space",
        shortDescription = "Text to vector conversion",
        longDescription = "Convert text into high-dimensional vectors using OpenAI's models.<br>Essential foundation for semantic search with detailed analysis and visualization.",
        icon = "/images/openai.svg",
        emoji = "🔢",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/embeddings"
    ),

    CHUNKING(
        id = "chunking",
        title = "Text Slicer",
        shortDescription = "Text segmentation tool",
        longDescription = "Interactive chunking tool with customizable size and overlap settings.<br>Essential for preparing large texts for AI processing and RAG systems.",
        icon = "bi-scissors",
        emoji = "✂️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "/demo/chunking"
    ),

    POLY_GLOT(
        id = "poly-glot",
        title = "PolyGlot",
        shortDescription = "Contextual translation bot",
        longDescription = "Translate between dozens of languages while maintaining conversation flow and cultural nuances.<br>Node.js/JavaScript app with Socket.io real-time chat.",
        icon = "bi-translate",
        emoji = "🌍",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.CORE_AI,
        route = "https://polyglot-vbh0.onrender.com/",
        soloProject = true,
        contentType = EXTERNAL_JS
    ),

    // Data Demos
    STOCK_DATA(
        id = "stock",
        title = "Stock Oracle",
        shortDescription = "AI-powered financial analysis",
        longDescription = "Fetch real-time stock data from Polygon.io and generate intelligent market analysis.<br>Complete pipeline from data retrieval to AI-driven insights.",
        icon = "bi-graph-up",
        emoji = "📈",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/stock"
    ),

    DODGY_DAVE(
        id = "dodgy-dave",
        title = "DodgyDave",
        shortDescription = "A stock tips chatbot",
        longDescription = "Give the app a couple of tickers and it'll tell you what to do - buy or sell!<br>Vite/JavaScript app with Socket.io real-time chat.",
        icon = "bi-graph-down",
        emoji = "📉",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "https://aieng.rodland.no/",
        soloProject = true,
        contentType = EXTERNAL_JS
    ),

    VECTOR_DATABASE(
        id = "supabase",
        title = "Vector Vault",
        shortDescription = "Semantic search with embeddings",
        longDescription = "Explore semantic search using embeddings and PostgreSQL pgvector extension.<br>Foundation for RAG systems using Supabase as the vector store.",
        icon = "bi-database",
        emoji = "🗄️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/supabase"
    ),

    RAG(
        id = "rag",
        title = "RAG Engine",
        shortDescription = "Retrieval-Augmented Generation",
        longDescription = "Advanced RAG implementation from the Scrimba course.<br>Complete pipeline: query→embedding→vector search→context-aware AI response using movie/podcast database.",
        icon = "bi-link-45deg",
        emoji = "🔗",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.DATA,
        route = "/demo/rag"
    ),

    POPCHOICE(
        id = "popchoice",
        title = "PopChoice",
        shortDescription = "AI movie recommendations",
        longDescription = "Analyze preferences, mood, and viewing history to find the perfect film for any occasion.<br>Intelligent recommendations powered by preference learning.",
        icon = "bi-film",
        emoji = "🍿",
        status = DemoStatus.DREAMS,
        category = DemoCategory.DATA,
        soloProject = true
    ),

    // Open Source Models
    HUGGINGFACE_CLASSIFICATION(
        id = "huggingface-classification",
        title = "Text Classifier",
        shortDescription = "Open-source text classification",
        longDescription = "Classify text into custom categories using HuggingFace BART model.<br>Explore zero-shot classification with confidence scores and detailed analysis.",
        icon = "/images/huggingface.svg",
        emoji = "🏷️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-classification"
    ),

    HUGGINGFACE_SUMMARIZATION(
        id = "huggingface-summarization",
        title = "Text Squisher",
        shortDescription = "Open-source text summarization",
        longDescription = "Compress long texts into concise summaries using HuggingFace BART model.<br>Customizable length parameters for perfect summary control.",
        icon = "/images/huggingface.svg",
        emoji = "📄",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-summarization"
    ),

    OBJECT_DETECTION(
        id = "huggingface-object-detection",
        title = "Object Spotter",
        shortDescription = "Visual object recognition",
        longDescription = "Detect and locate objects in images using DETR (Detection Transformer) model.<br>HuggingFace-powered vision with bounding boxes and confidence scores.",
        icon = "/images/huggingface.svg",
        emoji = "🔍",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.OPEN_SOURCE,
        route = "/demo/huggingface-object-detection"
    ),

    // Vision & Image
    FILM_FUSION(
        id = "film-fusion",
        title = "Poster Lab",
        shortDescription = "AI-generated movie posters",
        longDescription = "Combine cinematic imagery with artistic styles using advanced image generation.<br>Fine-tune parameters, models, and quality settings for stunning poster results.",
        icon = "bi-film",
        emoji = "🎬",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/film-fusion"
    ),

    IMAGE_EDITING(
        id = "image-editing",
        title = "Image Remix",
        shortDescription = "Mask-based image editing",
        longDescription = "Edit existing images with DALL-E 2 by describing what you want to change.<br>Precise mask-based editing for specific areas and seamless modifications.",
        icon = "bi-brush",
        emoji = "✏️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/image-editing"
    ),

    GPT4_VISION(
        id = "gpt4-vision",
        title = "Vision Quest",
        shortDescription = "Multi-modal image analysis",
        longDescription = "Analyze images with GPT-4 Vision using our curated gallery of demo images.<br>Explore AI's visual understanding with suggested prompts and detailed insights.",
        icon = "bi-eye",
        emoji = "👁️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.VISION_IMAGE,
        route = "/demo/gpt4-vision"
    ),

    // AI Agents
    REACT_AGENT(
        id = "react",
        title = "ReAct Playground",
        shortDescription = "Reasoning + Acting pattern",
        longDescription = "AI agent that solves problems through iterative thinking and action.<br>Watch step-by-step reasoning as the agent plans, acts, and learns from results.",
        icon = "bi-cpu",
        emoji = "🧠",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.AI_AGENTS,
        route = "/demo/react"
    ),

    FUNCTION_CALLING(
        id = "function-calling",
        title = "Tool Master",
        shortDescription = "Native OpenAI tool integration",
        longDescription = "AI directly calls external tools using structured function schemas.<br>Compare OpenAI's native approach with ReAct's manual reasoning patterns.",
        icon = "bi-gear",
        emoji = "🛠️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.AI_AGENTS,
        route = "/demo/function-calling"
    ),

    ASSISTANTS_API(
        id = "assistants-api",
        title = "Assistant Builder",
        shortDescription = "OpenAI's persistent assistants",
        longDescription = "Build persistent AI assistants using OpenAI's Assistants API with file handling, code execution, and long-term memory.<br>Advanced stateful assistants with tool capabilities beyond simple chat interactions.",
        icon = "bi-robot",
        emoji = "🤖",
        status = DemoStatus.COFFEE,
        category = DemoCategory.AI_AGENTS
    ),

    TRAVEL_AGENT(
        id = "trip-planner",
        title = "Trip Wizard",
        shortDescription = "Parallel agent trip planning",
        longDescription = "Multi-agent system using parallel processing to plan comprehensive trips.<br>Research, weather, activities, and food agents working together in real-time.",
        icon = "bi-airplane",
        emoji = "✈️",
        courseContent = false,
        status = DemoStatus.COMPLETED,
        category = DemoCategory.AI_AGENTS,
        route = "/demo/trip-planner"
    ),

    // MCP Protocol (Additional Explorations)
    MCP_SERVER(
        id = "mcp-server",
        title = "MCP Toolbox",
        shortDescription = "Custom AI tool server",
        longDescription = "Expose tools and resources that Claude Desktop can discover and use automatically.<br>Create your own AI tool ecosystem using Spring AI and MCP protocol.",
        icon = "bi-server",
        emoji = "🛠️",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.MCP_PROTOCOL,
        route = "/demo/mcp-server",
        courseContent = false,
    ),

    MCP_PROTOCOL_DEMO(
        id = "mcp-protocol",
        title = "MCP Decoded",
        shortDescription = "Protocol fundamentals",
        longDescription = "Educational demo showing JSON-RPC 2.0 calls, tool discovery, and parameter schemas.<br>Learn Model Context Protocol mechanics step-by-step.",
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
        title = "MCP Wizard",
        shortDescription = "Dynamic tool discovery",
        longDescription = "AI assistant that dynamically discovers and uses external tools via MCP.<br>Chat interface with OpenAI function calling and persistent session memory.",
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
        title = "LangChain Lab",
        shortDescription = "Declarative AI services",
        longDescription = "Test text analysis, summarization, and sentiment classification using annotated interfaces.<br>Compare LangChain4j's approach with our custom implementations.",
        icon = "/images/langchain.svg",
        emoji = "🦜",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.FRAMEWORKS,
        route = "/demo/langchain4j",
        courseContent = false
    ),

    SPRING_AI(
        id = "spring-ai",
        title = "Spring AI",
        shortDescription = "Native Spring integration",
        longDescription = "Spring's AI integration with auto-configuration, dependency injection, and native patterns.<br>Compare against LangChain4j and custom implementations.",
        icon = "/images/spring.svg",
        emoji = "🍃",
        status = DemoStatus.COMPLETED,
        category = DemoCategory.FRAMEWORKS,
        route = "/demo/spring-ai",
        courseContent = false
    ),


    // Additional Explorations (Future)
    MCP_REGISTRY(
        id = "mcp-registry",
        title = "MCP Catalog",
        shortDescription = "Server discovery service",
        longDescription = "Browse, connect, and manage multiple MCP servers with a unified interface.<br>Central registry for discovering and organizing AI tool ecosystems.",
        icon = "bi-globe",
        emoji = "\uD83C\uDF0D",
        status = DemoStatus.WILD_IDEAS,
        category = DemoCategory.MCP_PROTOCOL,
        courseContent = false
    ),

    OPENAI_JAVA(
        id = "openai-java",
        title = "OpenAI Toolkit",
        shortDescription = "Official Java/Kotlin SDK",
        longDescription = "Type-safe builders, streaming support, and structured outputs using OpenAI's official library.<br>Compare with our custom HTTP implementation approach.",
        icon = "bi-cup-hot",
        emoji = "☕",
        status = DemoStatus.PROCRASTINATING,
        category = DemoCategory.FRAMEWORKS,
        courseContent = false
    );

    fun external(): Boolean = route?.startsWith("http") ?: false

    val iconHtml: String = iconHtml(icon, title)

    companion object {
        fun getByCategory(category: DemoCategory): List<Demo> {
            return entries.filter { it.category == category }
        }

        fun getByNavbarGroup(navbarGroup: NavbarGroup): List<Demo> {
            return entries.filter { it.category.navbarGroup == navbarGroup }
        }
    }
}