import { MongoClient, ObjectId } from "mongodb";
import { faker } from "@faker-js/faker";
import { Command } from "commander";
import dotenv from "dotenv";

dotenv.config();

const defaultUsers = ["corey", "alejandro", "steve", "alayna"];

// Set up command line argument parsing
const program = new Command();
program
  .name("nexus-data-generator")
  .description("Generate fake social media data for the Nexus Social platform")
  .version("1.0.0")
  .option("-u, --users <number>", "Number of users to create", "50")
  .option(
    "-m, --messages-per-user <number>",
    "Average number of messages per user",
    "4"
  )
  .option(
    "-d, --friendship-density <number>",
    "Friendship connection density (0-1)",
    "0.3"
  )
  .option(
    "-a, --acceptance-rate <number>",
    "Friendship acceptance rate (0-1)",
    "0.7"
  )
  .option("--mongo-url <string>", "MongoDB connection URL")
  .option("--database <string>", "Database name")
  .option("--debug", "Enable debug mode with verbose logging")
  .parse();

const options = program.opts();

// Debug logging utility
const debugLog = (message, data = null) => {
  if (options.debug) {
    console.log(`[DEBUG] ${message}`, data || "");
  }
};

// Global error handler
process.on("uncaughtException", (error) => {
  console.error("[FATAL ERROR] Uncaught Exception:", error);
  process.exit(1);
});

process.on("unhandledRejection", (reason, promise) => {
  console.error(
    "[FATAL ERROR] Unhandled Rejection at:",
    promise,
    "reason:",
    reason
  );
  process.exit(1);
});

// Input validation
function validateInputs(opts) {
  const users = parseInt(opts.users);
  const messagesPerUser = parseFloat(opts.messagesPerUser);
  const friendshipDensity = parseFloat(opts.friendshipDensity);
  const acceptanceRate = parseFloat(opts.acceptanceRate);

  if (isNaN(users) || users < 1) {
    throw new Error("Users count must be a positive integer");
  }
  if (isNaN(messagesPerUser) || messagesPerUser < 0) {
    throw new Error("Messages per user must be a non-negative number");
  }
  if (
    isNaN(friendshipDensity) ||
    friendshipDensity < 0 ||
    friendshipDensity > 1
  ) {
    throw new Error("Friendship density must be between 0 and 1");
  }
  if (isNaN(acceptanceRate) || acceptanceRate < 0 || acceptanceRate > 1) {
    throw new Error("Acceptance rate must be between 0 and 1");
  }

  return { users, messagesPerUser, friendshipDensity, acceptanceRate };
}

// Validate inputs
const validated = validateInputs(options);

// MongoDB connection configuration
const MONGO_URL =
  options.mongoUrl || process.env.MONGO_URL || "mongodb://localhost:27017";
const DATABASE_NAME =
  options.database || process.env.DATABASE_NAME || "nexusSocial";

// Data generation configuration from arguments
const CONFIG = {
  users: validated.users,
  messagesPerUser: validated.messagesPerUser,
  friendshipDensity: validated.friendshipDensity,
  acceptanceRate: validated.acceptanceRate,
  get posts() {
    return Math.floor(this.users * this.messagesPerUser);
  },
  get friendRequests() {
    return Math.floor(
      ((this.users * (this.users - 1)) / 2) * this.friendshipDensity
    );
  },
};

class MongoDataLoader {
  constructor(config) {
    this.client = null;
    this.db = null;
    this.config = config;
  }

  async connect() {
    try {
      console.log("Connecting to MongoDB...");
      debugLog("MongoDB URL:", MONGO_URL);
      debugLog("Database name:", DATABASE_NAME);

      this.client = new MongoClient(MONGO_URL);
      debugLog("MongoClient created");

      await this.client.connect();
      debugLog("Client connected successfully");

      this.db = this.client.db(DATABASE_NAME);
      debugLog("Database reference obtained");

      // Test the connection
      await this.db.admin().ping();
      debugLog("Database ping successful");

      console.log(`Connected to MongoDB database: ${DATABASE_NAME}`);
    } catch (error) {
      console.error("Error connecting to MongoDB:", error);
      debugLog("Connection error details:", error);
      throw error;
    }
  }

  async disconnect() {
    if (this.client) {
      await this.client.close();
      console.log("Disconnected from MongoDB");
    }
  }

  generateUsers(count) {
    const users = [];
    const usernames = new Set(); // Ensure unique usernames

    for (let i = 0; i < count; i++) {
      let username = faker.internet.username().toLowerCase();

      // Ensure unique username
      while (usernames.has(username)) {
        username =
          faker.internet.username().toLowerCase() +
          faker.number.int({ min: 1, max: 999 });
      }
      usernames.add(username);

      const user = {
        _id: new ObjectId(),
        username: username,
        email: faker.internet.email().toLowerCase(),
        firstName: faker.person.firstName(),
        lastName: faker.person.lastName(),
        phone: faker.phone.number(),
        avatar: faker.image.avatar(),
        password: "���W\u000b~���\u0016�%�\u0001U�\u0014��9@hy\f��B0\u0000p", // Use the same password hash for all users "nexus"
        bio: faker.lorem.sentence(),
        _class: "clarity.internship.backend_api.models.User",
      };

      if (i < 4) {
        //Set us as the first four users!
        user.username = defaultUsers[i];
      }

      users.push(user);
    }

    return users;
  }

  generatePosts(count, users) {
    const posts = [];

    for (let i = 0; i < count; i++) {
      const author = faker.helpers.arrayElement(users);
      const postTimestamp = faker.date.recent({ days: 30 });

      // Generate likes (0-30% of users might like a post)
      const likeProbability = faker.number.float({ min: 0, max: 0.3 });
      const likedBy = users
        .filter((user) => user.username !== author.username) // Author can't like their own post
        .filter(() => faker.datatype.boolean({ probability: likeProbability }))
        .map((user) => user.username);

      // Generate comments (0-5 comments per post)
      const numComments = faker.number.int({ min: 0, max: 5 });
      const comments = [];

      for (let j = 0; j < numComments; j++) {
        const commenter = faker.helpers.arrayElement(users);

        // Comments happen after the post timestamp
        const commentTimestamp = faker.date.between({
          from: postTimestamp,
          to: new Date(),
        });

        comments.push({
          author: commenter.username,
          authorAvatar: commenter.avatar,
          text: faker.lorem.sentence(),
          timestamp: commentTimestamp,
        });
      }

      // Sort comments by timestamp
      comments.sort((a, b) => a.timestamp - b.timestamp);

      const post = {
        _id: new ObjectId(),
        authorId: author.username,
        content: faker.lorem.paragraphs({ min: 1, max: 3 }),
        authorAvatar: author.avatar,
        timestamp: postTimestamp,
        likedBy: likedBy,
        comments: comments,
        _class: "clarity.internship.backend_api.models.Post",
      };

      posts.push(post);
    }

    return posts;
  }

  generateFriendRequests(count, usernames, acceptanceRate) {
    const friendRequests = [];
    const requestPairs = new Set(); // Avoid duplicate friend requests

    for (let i = 0; i < count; i++) {
      const requester = faker.helpers.arrayElement(usernames);
      let recipient = faker.helpers.arrayElement(usernames);

      // Ensure requester and recipient are different
      while (recipient === requester) {
        recipient = faker.helpers.arrayElement(usernames);
      }

      const pairKey = `${requester}-${recipient}`;
      const reversePairKey = `${recipient}-${requester}`;

      // Skip if this pair already exists
      if (requestPairs.has(pairKey) || requestPairs.has(reversePairKey)) {
        continue;
      }

      requestPairs.add(pairKey);

      // Use the provided acceptance rate
      const isAccepted = faker.datatype.boolean({
        probability: acceptanceRate,
      });
      const isRejected =
        !isAccepted && faker.datatype.boolean({ probability: 0.2 }); // 20% of non-accepted are rejected

      const friendRequest = {
        _id: new ObjectId(),
        requestingUserId: requester,
        requestRecipientId: recipient,
        requestTimestamp: faker.date.recent({ days: 7 }).toISOString(),
        accepted: isAccepted,
        rejected: isRejected,
        _class: "clarity.internship.backend_api.models.FriendRequest",
      };

      friendRequests.push(friendRequest);
    }

    return friendRequests;
  }

  async insertUsers() {
    try {
      console.log(`Generating ${this.config.users} fake users...`);
      const users = this.generateUsers(this.config.users);

      const collection = this.db.collection("users");

      // Clear existing data
      await collection.deleteMany({});
      console.log("Cleared existing users collection");

      // Insert new data
      const result = await collection.insertMany(users);
      console.log(`Inserted ${result.insertedCount} users`);

      // Return full user objects for use in other collections
      return users;
    } catch (error) {
      console.error("Error inserting users:", error);
      throw error;
    }
  }

  async insertPosts(users) {
    try {
      console.log(`Generating ${this.config.posts} fake posts...`);
      const posts = this.generatePosts(this.config.posts, users);

      const collection = this.db.collection("posts");

      // Clear existing data
      await collection.deleteMany({});
      console.log("Cleared existing posts collection");

      // Insert new data
      const result = await collection.insertMany(posts);
      console.log(`Inserted ${result.insertedCount} posts`);
    } catch (error) {
      console.error("Error inserting posts:", error);
      throw error;
    }
  }

  async insertFriendRequests(users) {
    try {
      console.log(
        `Generating ${this.config.friendRequests} fake friend requests...`
      );
      const usernames = users.map((user) => user.username);
      const friendRequests = this.generateFriendRequests(
        this.config.friendRequests,
        usernames,
        this.config.acceptanceRate
      );

      const collection = this.db.collection("friends");

      // Clear existing data
      await collection.deleteMany({});
      console.log("Cleared existing friendRequests collection");

      // Insert new data
      const result = await collection.insertMany(friendRequests);
      console.log(`Inserted ${result.insertedCount} friend requests`);
    } catch (error) {
      console.error("Error inserting friend requests:", error);
      throw error;
    }
  }

  async loadAllData() {
    try {
      await this.connect();

      console.log("Starting fake data generation process...");

      // Generate users first and get user objects for other collections
      const users = await this.insertUsers();

      // Generate posts and friend requests using the user objects
      await this.insertPosts(users);
      await this.insertFriendRequests(users);

      console.log("Fake data generation completed successfully!");
    } catch (error) {
      console.error("Error during data generation:", error);
      throw error;
    } finally {
      await this.disconnect();
    }
  }
}

// Main execution
async function main() {
  console.log("Configuration:");
  console.log(`  Users: ${CONFIG.users}`);
  console.log(`  Messages per user: ${CONFIG.messagesPerUser}`);
  console.log(`  Total posts: ${CONFIG.posts}`);
  console.log(`  Friendship density: ${CONFIG.friendshipDensity}`);
  console.log(`  Total friend requests: ${CONFIG.friendRequests}`);
  console.log(`  Acceptance rate: ${CONFIG.acceptanceRate}`);
  console.log(`  MongoDB URL: ${MONGO_URL}`);
  console.log(`  Database: ${DATABASE_NAME}`);
  console.log("");

  const loader = new MongoDataLoader(CONFIG);

  try {
    await loader.loadAllData();
    process.exit(0);
  } catch (error) {
    console.error("Application failed:", error);
    process.exit(1);
  }
}

// Run the application
if (import.meta.url === `file://${process.argv[1]}`) {
  main();
}

export default MongoDataLoader;
