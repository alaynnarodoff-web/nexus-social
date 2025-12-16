# Nexus Social Chat Responder

An AI-powered chatbot that monitors the Nexus Social platform and generates intelligent responses to user posts.

## Features

- 🔄 **Automatic Polling**: Monitors new posts every 60 seconds (configurable)
- 🤖 **AI Responses**: Uses Hugging Face's free Mistral API for intelligent responses
- 💬 **Smart Commenting**: Adds comments as predefined bot users instead of creating new posts
- 🎯 **Smart Filtering**: Configurable response rate to avoid spam
- 📊 **Enhanced Local Responses**: Contextual responses when AI APIs are unavailable
- 📊 **MongoDB Integration**: Seamlessly works with existing Nexus Social database

## Setup

### 1. Install Dependencies

```bash
cd chat-responder
npm install
```

### 2. Environment Configuration

Create a `.env` file in the chat-responder directory:

```bash
# MongoDB Configuration
MONGO_URL=mongodb://localhost:27017
DATABASE_NAME=nexusSocial

# Hugging Face API Configuration (get free key at https://huggingface.co/settings/tokens)
HUGGINGFACE_API_KEY=your_huggingface_api_key_here
USE_LOCAL_ONLY=false
AI_API_URL=https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.1

# Polling Configuration
POLL_INTERVAL_SECONDS=60
LOOKBACK_SECONDS=60

# Chatbot Configuration
RESPONSE_RATE=0.3
```

### 3. Run the Application

```bash
# Production mode
npm start

# Development mode (auto-restart on changes)
npm run dev
```

## How It Works

1. **Polling**: Every 60 seconds, checks for new posts in the last 60 seconds
2. **Filtering**: Only responds to ~30% of posts (configurable) to avoid being too chatty
3. **AI Generation**: Sends post content to external AI API for response generation
4. **Fallback**: If AI fails, uses contextual predefined responses
5. **Response Posting**: Creates new post in database as AI assistant user

## API Integration

### Free AI APIs Supported

1. **Hugging Face Inference API** (Default)

   - No API key required for basic usage
   - Rate limited but free
   - Models: DialoGPT, BlenderBot, etc.

2. **Alternative Free APIs**
   - Replicate (with free tier)
   - Cohere (with free tier)
   - OpenAI compatible endpoints

### Adding Custom AI APIs

Modify the `callAIAPI` method in `app.js` to integrate with your preferred AI service:

```javascript
async callAIAPI(apiUrl, postContent, authorName) {
  // Your custom API integration here
}
```

## Configuration Options

| Variable                | Default              | Description                         |
| ----------------------- | -------------------- | ----------------------------------- |
| `POLL_INTERVAL_SECONDS` | 60                   | How often to check for new posts    |
| `LOOKBACK_SECONDS`      | 60                   | How far back to look for posts      |
| `RESPONSE_RATE`         | 0.3                  | Probability of responding (0.0-1.0) |
| `CHATBOT_USERNAME`      | nexus_ai_assistant   | Bot's username                      |
| `AI_API_URL`            | HuggingFace DialoGPT | Primary AI API endpoint             |

## Monitoring

The application provides detailed console logging:

- 🔌 Connection status
- 🔍 Posts found and processed
- 🎯 Response generation attempts
- ✅ Successful responses posted
- ❌ Error handling

## Troubleshooting

### Common Issues

1. **AI API Rate Limiting**: Switch to fallback responses or reduce response rate
2. **MongoDB Connection**: Verify connection string and database access
3. **No Responses Generated**: Check response rate and recent post availability

### Debug Mode

Enable verbose logging by modifying the console.log statements or adding a DEBUG environment variable.

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   MongoDB       │    │  Chat Responder  │    │   External AI   │
│   (Posts)       │◄──►│     Service      │◄──►│      API        │
│                 │    │                  │    │  (Hugging Face) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
        ▲                       │
        │                       ▼
        └───────────────────────────
            Posts AI Response
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Test with your MongoDB instance
4. Submit a pull request

## License

ISC License - see package.json for details.
