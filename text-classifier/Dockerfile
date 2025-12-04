FROM python:3.10-slim

# Install system dependencies
RUN apt-get update && apt-get install -y git && apt-get clean

WORKDIR /app

COPY requirements.txt .

# Upgrade pip and install requirements with NumPy compatibility
RUN pip install --upgrade pip && pip install --no-cache-dir -r requirements.txt

COPY app.py .

# Expose FastAPI default port
EXPOSE 8000

# Start the API with proper logging
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000", "--log-level", "info", "--access-log"]

