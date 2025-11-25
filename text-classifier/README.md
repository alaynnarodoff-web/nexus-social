# Text Classifier API

A FastAPI-based text classification service with sentiment analysis and named entity recognition capabilities.

## Features

- **Text Classification**: Zero-shot classification using BART model with customizable categories
- **Sentiment Analysis**: Sentiment detection with confidence scores
- **Named Entity Recognition**: Extract and optionally expand entities using contextual analysis

## Requirements

- Python 3.9+ (recommended due to PyTorch compatibility)
- Docker (optional)

## Installation & Setup

### Option 1: Local Python Environment

1. **Clone/Navigate to the project directory:**

   ```bash
   cd text-classifier
   ```

2. **Create and activate virtual environment:**

   ```bash
   python3.9 -m venv .venv
   source .venv/bin/activate  # On macOS/Linux
   # or
   .venv\Scripts\activate     # On Windows
   ```

3. **Install dependencies:**

   ```bash
   pip install --upgrade pip
   pip install -r requirements.txt
   ```

4. **Run the application:**
   ```bash
   python -m uvicorn app:app --reload --host 0.0.0.0 --port 8000
   ```

### Option 2: Docker

1. **Build the Docker image:**

   ```bash
   docker build -t text-classifier .
   ```

2. **Run the container:**
   ```bash
   docker run -p 8000:8000 text-classifier
   ```

### API Endpoints

#### Health Check

```bash
curl -X GET "http://localhost:8000/healthcheck"
```

#### Text Classification

```bash
curl -X POST "http://localhost:8000/classify" \
     -H "Content-Type: application/json" \
     -d '{
       "text": "Your text here",
       "labels": ["optional", "custom", "categories"],
       "include_sentiment": true,
       "include_entities": true,
       "contextual_entities": true
     }'
```

### Example Requests

**Basic classification:**

```bash
curl -X POST "http://localhost:8000/classify" \
     -H "Content-Type: application/json" \
     -d '{"text": "The stock market reached record highs today"}'
```

**Custom categories:**

```bash
curl -X POST "http://localhost:8000/classify" \
     -H "Content-Type: application/json" \
     -d '{
       "text": "I love playing video games",
       "labels": ["gaming", "entertainment", "technology", "sports"]
     }'
```

## API Parameters

| Parameter           | Type    | Default            | Description                      |
| ------------------- | ------- | ------------------ | -------------------------------- |
| `text`              | string  | **required**       | Text to analyze                  |
| `labels`            | array   | Default categories | Custom classification categories |
| `include_sentiment` | boolean | `true`             | Include sentiment analysis       |
| `include_entities`  | boolean | `true`             | Include named entity recognition |

## Default Categories

The API includes these default classification categories:

- technology, politics, sports, business, health, law, local news, video games
- entertainment, science, education, adventure, social media, movies, books
- travel, food, environment, history, art, music, fashion, finance

## Response Format

```json
{
  "text": "Your input text",
  "classification": [
    { "category": "politics", "score": 0.892 },
    { "category": "business", "score": 0.076 },
    { "category": "technology", "score": 0.032 }
  ],
  "sentiment": {
    "label": "positive",
    "score": 0.934
  },
  "entities": [
    {
      "text": "Donald Trump",
      "label": "PER",
      "confidence": 0.999,
      "original": "Donald",
      "expansion_method": "text_analysis"
    }
  ]
}
```

## Development Commands

**Install development dependencies:**

```bash
pip install --upgrade pip safetensors
```

**Run with auto-reload (development):**

```bash
python -m uvicorn app:app --reload --host 0.0.0.0 --port 8000
```

### Model Loading Errors

The app will automatically fall back to default models if advanced models fail to load. Check the logs for details.

## Docker Commands Summary

```bash
# Build image
docker build -t text-classifier .

# Run container
docker run -p 8000:8000 text-classifier

# Run in background
docker run -d -p 8000:8000 text-classifier

# View logs
docker logs <container-id>

# Stop container
docker stop <container-id>
```

## Project Structure

```
text-classifier/
├── app.py              # Main FastAPI application
├── requirements.txt    # Python dependencies
├── Dockerfile         # Docker configuration
└── README.md          # This file
```
