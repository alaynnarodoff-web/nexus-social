import { MongoClient } from "mongodb";
import dotenv from "dotenv";
import cron from "node-cron";
import axios from "axios";

dotenv.config();

// Configuration
const config = {
  mongodb: {
    url: process.env.MONGO_URL || "mongodb://localhost:27017",
    database: process.env.DATABASE_NAME || "nexusSocial",
  },
  ai: {
    // Local Ollama API with phi3 model
    apiUrl: process.env.AI_API_URL || "http://localhost:11434/api/chat",
    model: process.env.AI_MODEL || "phi3", // Local Ollama model
    useLocalOnly: process.env.USE_LOCAL_ONLY === "true" || false,
  },
  polling: {
    intervalSeconds: parseInt(process.env.POLL_INTERVAL_SECONDS) || 10,
    lookbackSeconds: parseInt(process.env.LOOKBACK_SECONDS) || 10,
  },
  chatbot: {
    responseRate: parseFloat(process.env.RESPONSE_RATE) || 0.9,
    users: [
      {
        username: "herman_olson68",
        avatar:
          "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/15.jpg",
      },
      {
        username: "kailee.bauch",
        avatar:
          "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/23.jpg",
      },
      {
        username: "evalyn_wunsch82",
        avatar:
          "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/male/512/42.jpg",
      },
      {
        username: "unique.stroman-mante",
        avatar:
          "https://cdn.jsdelivr.net/gh/faker-js/assets-person-portrait/female/512/67.jpg",
      },
    ],
  },
};

class ChatResponder {
  constructor() {
    this.client = null;
    this.db = null;
    this.isRunning = false;
  }

  async connect() {
    try {
      console.log("Connecting to MongoDB...");
      this.client = new MongoClient(config.mongodb.url);
      await this.client.connect();
      this.db = this.client.db(config.mongodb.database);

      // Test connection
      await this.db.admin().ping();
      console.log(`Connected to MongoDB database: ${config.mongodb.database}`);
    } catch (error) {
      console.error("Error connecting to MongoDB:", error);
      throw error;
    }
  }

  async disconnect() {
    if (this.client) {
      await this.client.close();
      console.log("Disconnected from MongoDB");
    }
  }

  async getRecentPosts() {
    try {
      const cutoffTime = new Date(
        Date.now() - config.polling.lookbackSeconds * 1000
      );

      const botUsernames = config.chatbot.users.map((user) => user.username);

      const posts = await this.db
        .collection("posts")
        .find({
          timestamp: { $gte: cutoffTime },
          // Don't respond to our own posts
          authorId: { $nin: botUsernames },
        })
        .sort({ timestamp: -1 })
        .toArray();

      console.log(
        `Found ${posts.length} recent posts since ${cutoffTime.toISOString()}`
      );
      return posts;
    } catch (error) {
      console.error("Error fetching recent posts:", error);
      return [];
    }
  }

  async generateResponse(postContent, comments) {
    try {
      if (!config.ai.useLocalOnly) {
        const response = await this.callAIAPI(
          config.ai.apiUrl,
          postContent,
          comments
        );
        if (response) {
          return response;
        }
      }

      // Fall back to local responses
      console.log("Using enhanced local responses");
      return this.generateEnhancedResponse(postContent);
    } catch (error) {
      console.error("Error generating response:", error);
      return this.generateEnhancedResponse(postContent);
    }
  }

  async callAIAPI(apiUrl, postContent, comments = []) {
    if (config.ai.useLocalOnly) {
      console.log("Using local responses only");
      return null;
    }

    try {
      const userMessage = `Please create a twitter-like response to the post: ${postContent} which has the following comments: ${comments
        .map((c) => c.text)
        .join(
          " | "
        )}. Don't mention anything about being an AI or a bot. Keep it concise and engaging.`;
      console.log(`Making Ollama API call to phi3...`);

      const response = await axios.post(
        apiUrl,
        {
          model: "phi3",
          messages: [
            {
              role: "user",
              content: userMessage,
            },
          ],
          stream: false,
        },
        {
          headers: {
            "Content-Type": "application/json",
          },
          timeout: 120000, // 2 minutes for local Ollama
        }
      );

      console.log("Ollama API response status:", response.status);

      if (
        response.data &&
        response.data.message &&
        response.data.message.content
      ) {
        const result = response.data.message.content;
        console.log(`Ollama phi3 response: "${result.substring(0, 50)}..."`);
        return result.trim();
      }

      console.log("Unexpected Ollama API response:", response.data);
      return null;
    } catch (error) {
      console.error(`Ollama API failed:`, error.message);
      if (error.code === "ECONNREFUSED") {
        console.log("Ollama server not running, will use local response");
      } else if (error.response?.status === 404) {
        console.log("Model phi3 not found, will use local response");
      }
      return null;
    }
  }

  generateEnhancedResponse(postContent) {
    const content = postContent.toLowerCase();

    // Contextual responses based on content analysis
    const patterns = {
      // Questions
      question: /\?|what|how|why|when|where|who|should i|can i|anyone/,
      // Gratitude
      gratitude: /thank|grateful|appreciate|blessed/,
      // Excitement/Positive
      excitement:
        /excited|amazing|awesome|great|love|happy|wonderful|fantastic/,
      // Challenges/Negative
      challenge: /tired|stressed|difficult|hard|tough|struggling|problem/,
      // Food
      food: /coffee|food|eat|drink|recipe|cook|meal|lunch|dinner|breakfast/,
      // Travel
      travel: /travel|trip|vacation|visit|airport|flight|hotel/,
      // Work/Career
      work: /work|job|meeting|project|boss|colleague|office/,
      // Weather
      weather: /weather|rain|sunny|cold|hot|snow|storm/,
      // Social
      social: /friend|family|party|event|together|meet/,
      // Learning
      learning: /learn|study|book|read|class|school|university/,
    };

    const responses = {
      question: [
        "That's a really good question!",
        "I've wondered about that too!",
        "Interesting question - what do you think?",
        "Good point to consider!",
      ],
      gratitude: [
        "That's such a positive way to look at things!",
        "Gratitude makes such a difference!",
        "Love this perspective!",
        "Thanks for the reminder to appreciate the little things!",
      ],
      excitement: [
        "Your enthusiasm is contagious!",
        "So happy for you!",
        "That sounds absolutely wonderful!",
        "Love seeing posts like this!",
      ],
      challenge: [
        "Hope things get easier for you soon!",
        "You've got this - one step at a time!",
        "Sending positive thoughts your way!",
        "Tomorrow is a new day!",
      ],
      food: [
        "Now I'm craving that too!",
        "Sounds delicious!",
        "Food always makes everything better!",
        "Great choice!",
      ],
      travel: [
        "Travel creates the best memories!",
        "Safe travels!",
        "That sounds like an adventure!",
        "Hope you're having a great time!",
      ],
      work: [
        "Hope your day goes smoothly!",
        "Good luck with everything!",
        "You're doing great!",
        "Keep up the good work!",
      ],
      weather: [
        "Weather definitely affects the mood!",
        "Hope you're staying comfortable!",
        "Perfect weather for relaxing!",
        "Nature never fails to amaze!",
      ],
      social: [
        "Time with good people is the best!",
        "Sounds like fun!",
        "Hope you all have a great time!",
        "Those are the moments that matter!",
      ],
      learning: [
        "Learning something new is always exciting!",
        "Knowledge is power!",
        "Good for you for expanding your mind!",
        "What's your favorite thing you've learned recently?",
      ],
    };

    // Find matching pattern
    for (const [category, pattern] of Object.entries(patterns)) {
      if (pattern.test(content)) {
        const categoryResponses = responses[category];
        return categoryResponses[
          Math.floor(Math.random() * categoryResponses.length)
        ];
      }
    }

    // Generic positive responses
    const generic = [
      "Thanks for sharing!",
      "This is great!",
      "I can relate to this!",
      "Really interesting perspective!",
      "Love this post!",
      "So true!",
      "This made my day!",
      "Couldn't agree more!",
    ];

    return generic[Math.floor(Math.random() * generic.length)];
  }

  async addComment(originalPost, responseText, user) {
    try {
      const newComment = {
        author: user.username,
        authorAvatar: user.avatar,
        text: responseText,
        timestamp: new Date(),
      };

      const result = await this.db
        .collection("posts")
        .updateOne(
          { _id: originalPost._id },
          { $push: { comments: newComment } }
        );

      console.log(
        `Added comment by ${user.username} to post by ${user.authorId}`
      );

      return result;
    } catch (error) {
      console.error("Error adding comment:", error);
      throw error;
    }
  }

  async processRecentPosts() {
    if (this.isRunning) {
      console.log("⏳ Previous poll still running, skipping...");
      return;
    }

    this.isRunning = true;

    try {
      console.log("\\nStarting poll cycle...");
      const recentPosts = await this.getRecentPosts();

      if (recentPosts.length === 0) {
        console.log("No new posts found. Waiting for next cycle...");
        return;
      }

      let responsesGenerated = 0;
      for (const post of recentPosts) {
        const comments = (post.comments || []).map((c) => c.text);
        for (const user of config.chatbot.users) {
          if (Math.random() > config.chatbot.responseRate) {
            console.log(
              `Skipping post by ${post.authorId} (response rate filter)`
            );
            continue;
          }

          // Check if any of our bots already commented on this post
          const hasExistingComment =
            post.comments &&
            post.comments.some((comment) => user.username === comment.author);

          if (hasExistingComment) {
            console.log(`Already commented on post by ${post.authorId}`);
            continue;
          }

          console.log(
            `Generating comment for post by ${
              post.authorId
            }: "${post.content.substring(0, 50)}..."`
          );

          const responseText = await this.generateResponse(
            post.content,
            comments
          );

          if (responseText) {
            comments.push(responseText);
            await this.addComment(post, responseText, user);
            responsesGenerated++;
          }
        }
      }

      console.log(`Poll cycle complete! Added ${responsesGenerated} comments.`);
    } catch (error) {
      console.error("❌ Error in poll cycle:", error);
    } finally {
      this.isRunning = false;
    }
  }

  async start() {
    try {
      await this.connect();

      console.log("\\nChat Responder Configuration:");
      console.log(`   Database: ${config.mongodb.database}`);
      console.log(`   Bot Username: ${config.chatbot.username}`);
      console.log(
        `   Poll Interval: ${config.polling.intervalSeconds} seconds`
      );
      console.log(
        `   Lookback Window: ${config.polling.lookbackSeconds} seconds`
      );
      console.log(
        `   Response Rate: ${(config.chatbot.responseRate * 100).toFixed(1)}%`
      );
      console.log(`   AI API: ${config.ai.apiUrl}`);
      console.log("\\nStarting chat responder...");

      // Run initial poll
      await this.processRecentPosts();

      // Schedule recurring polls
      const cronExpression = `*/${config.polling.intervalSeconds} * * * * *`;
      console.log(
        `Scheduling polls every ${config.polling.intervalSeconds} seconds...`
      );

      cron.schedule(cronExpression, async () => {
        await this.processRecentPosts();
      });

      console.log("✅ Chat responder is running! Press Ctrl+C to stop.");
    } catch (error) {
      console.error("❌ Failed to start chat responder:", error);
      process.exit(1);
    }
  }

  async stop() {
    console.log("\\n🛑 Stopping chat responder...");
    await this.disconnect();
    process.exit(0);
  }
}

// Handle graceful shutdown
process.on("SIGINT", async () => {
  console.log("\\n Received SIGINT signal");
  if (global.chatResponder) {
    await global.chatResponder.stop();
  } else {
    process.exit(0);
  }
});

process.on("SIGTERM", async () => {
  console.log("\\n Received SIGTERM signal");
  if (global.chatResponder) {
    await global.chatResponder.stop();
  } else {
    process.exit(0);
  }
});

// Main execution
async function main() {
  console.log("Nexus Social Chat Responder Starting...");

  const responder = new ChatResponder();
  global.chatResponder = responder;

  await responder.start();
}

// Run the application
if (import.meta.url === `file://${process.argv[1]}`) {
  main().catch(console.error);
}

export default ChatResponder;
