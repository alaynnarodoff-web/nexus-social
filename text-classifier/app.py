from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline
from typing import Optional, List
import logging
import re

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Text Classifier API", version="1.0.0")

logger.info("Loading all models...")
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

# Try loading sentiment and NER models with safetensors support
try:
    sentiment_analyzer = pipeline("sentiment-analysis", model="cardiffnlp/twitter-roberta-base-sentiment-latest", use_fast=True)
    logger.info("Sentiment model loaded successfully!")
except Exception as e:
    logger.warning(f"Could not load advanced sentiment model, using default: {e}")
    sentiment_analyzer = pipeline("sentiment-analysis")

try:
    ner_extractor = pipeline("ner", model="dbmdz/bert-large-cased-finetuned-conll03-english", aggregation_strategy="simple", use_fast=True)
    logger.info("NER model loaded successfully!")
except Exception as e:
    logger.warning(f"Could not load advanced NER model, using default: {e}")
    ner_extractor = pipeline("ner", aggregation_strategy="simple")

logger.info("All models loaded successfully!")

DEFAULT_LABELS = [
    "technology", "politics", "sports", "business", "health","law", "local news", "video games",
    "entertainment", "science", "education", "adventure", "social media", "movies", "books", "travel", "food", "environment", "history", "art", "music", "fashion", "finance"
]

class TextRequest(BaseModel):
    text: str
    labels: Optional[List[str]] = None
    include_sentiment: Optional[bool] = True
    include_entities: Optional[bool] = True

@app.get("/healthcheck")
def health_check():
    logger.info("Health check requested")
    return {"status": "healthy", "message": "Text Classifier API is running"}

@app.post("/classify")
def classify_text(req: TextRequest):
    logger.info(f"Classification request received for text: {req.text[:50]}...")
    labels = req.labels if req.labels else DEFAULT_LABELS
    logger.info(f"Using labels: {labels}")
    
    # Text classification
    result = classifier(req.text, candidate_labels=labels)
    
    # Get top 3 matches with their scores (ensure all values are JSON serializable)
    top_3_matches = []
    for i in range(min(3, len(result["labels"]))):
        top_3_matches.append({
            "category": str(result["labels"][i]),
            "score": float(round(result["scores"][i], 3))
        })
    
    response = {
        "text": str(req.text),
        "classification": top_3_matches
    }
    
    # Add sentiment analysis if requested
    if req.include_sentiment:
        try:
            sentiment_result = sentiment_analyzer(req.text)[0]
            response["sentiment"] = {
                "label": str(sentiment_result["label"]).lower(),
                "score": float(round(sentiment_result["score"], 3))
            }
        except Exception as e:
            logger.error(f"Sentiment analysis failed: {e}")
            response["sentiment"] = {
                "error": "Sentiment analysis failed"
            }
    
    # Add named entity recognition if requested
    if req.include_entities:
        try:
            entities = ner_extractor(req.text)
            response["entities"] = [
                {
                    "text": str(entity["word"]),
                    "label": str(entity["entity_group"]),
                    "confidence": float(round(entity["score"], 3))
                }
                for entity in entities if float(entity["score"]) > 0.5  # Filter low confidence entities
            ]
        except Exception as e:
            logger.error(f"Named entity recognition failed: {e}")
            response["entities"] = {
                "error": "Named entity recognition failed"
            }
    
    top_predictions = [f"{match['category']} ({match['score']})" for match in top_3_matches]
    logger.info(f"Analysis completed. Top 3: {top_predictions}")
    return response

