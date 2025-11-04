# MongoDB Fake Data Generator

This application connects to MongoDB and generates realistic fake data for the Nexus Social platform using Faker.js. It creates three collections: users, posts, and friendRequests with realistic relationships between them.

## Setup

1. Install dependencies:

   ```bash
   npm install
   ```

2. Make sure MongoDB is running (if using local instance):

## Usage

### Basic Usage

Run with default settings:

```bash
npm run load-data
```

Or:

```bash
node app.js
```

### Command Line Options

Generate custom amounts of data:

```bash
npm run load-data -u 100 -m 5 -d 0.4 -a 0.8
```

#### Available Options:

- `-u, --users <number>` - Number of users to create (default: 50)
- `-m, --messages-per-user <number>` - Average messages per user (default: 4)
- `-d, --friendship-density <number>` - Friendship connection density 0-1 (default: 0.3)
- `-a, --acceptance-rate <number>` - Friendship acceptance rate 0-1 (default: 0.7)
- `--mongo-url <string>` - MongoDB connection URL override
- `--database <string>` - Database name override
- `-h, --help` - Show help

#### Examples:

```bash
# Generate a small test dataset
npm run load-data -u 10 -m 2 -d 0.5 -a 0.9

# Generate a larger realistic dataset
npm run load-data -u 500 -m 8 -d 0.2 -a 0.6

# Connect to custom MongoDB instance
npm run load-data --mongo-url mongodb://localhost:27018 --database myTestDB
```

## What it does

The application will:

1. Connect to your MongoDB database
2. Clear existing data in the collections (users, posts, friendRequests)
3. Generate realistic fake data using Faker.js:
   - **Users**: Unique usernames, emails, names, phone numbers, avatars, and bios
   - **Posts**: Random content with timestamps, linked to generated users
   - **Friend Requests**: Realistic friendship connections between users
4. Ensure data relationships (posts reference real users, friend requests use real usernames)
5. Provide progress updates and error handling

## Configuration

Configuration can be set via command line arguments (recommended) or environment variables:

### Command Line Arguments (Preferred)

See Usage section above for all available options.

### Environment Variables (Alternative)

- `MONGO_URL`: MongoDB connection string (default: `mongodb://localhost:27017`)
- `DATABASE_NAME`: Database name (default: `nexusSocial`)

Note: Command line arguments take precedence over environment variables.

## Data Generation Logic

### Smart Calculations

- **Total Posts**: `users × messages_per_user`
- **Total Friend Requests**: `(users × (users-1) ÷ 2) × friendship_density`

### Generated Data Features

#### Users

- **Unique usernames**: No duplicate usernames across users
- **Complete profiles**: Names, emails, phone numbers, avatars, and bios
- **Consistent avatars**: Profile images used across posts and comments

#### Posts

- **Rich social interactions**: Each post includes likes and comments
- **Author information**: Posts include author username and avatar
- **Realistic engagement**: 0-30% of users may like each post
- **Dynamic comments**: 0-5 comments per post from different users
- **Chronological order**: Comments are sorted by timestamp

#### Social Features

- **Smart friend requests**: No self-requests or duplicate friend pairs
- **Configurable acceptance rates**: Use `--acceptance-rate` to control friendship acceptance
- **Friendship density control**: Use `--friendship-density` to control how connected users are
- **Realistic social behavior**: Users don't like their own posts
- **Recent timestamps**: Posts and friend requests from the last 30 days

## Collections Created

### Users Collection

- User profiles with authentication data
- Includes: username, email, names, phone, avatar, password, bio

### Posts Collection

- Social media posts with full engagement features
- Includes: content, author info, timestamps, likes array, comments array

### Friend Requests Collection

- Friend request records with acceptance status
- Includes: requesting user, recipient user, timestamps, status flags

## Troubleshooting

### PowerShell Issues

If the application runs without output or doesn't insert records in PowerShell:

#### 1. **Check MongoDB Connection**

```powershell
# Verify MongoDB is running
Get-Process mongod

# Test connection manually
mongosh --eval "db.adminCommand('ismaster')"
```

#### 2. **Enable Debug Mode**

```powershell
node app.js --debug -u 5
```

This will show detailed connection and operation logs.

#### 3. **Common PowerShell Fixes**

**Windows MongoDB Connection:**

```powershell
# Use Windows-style connection string
node app.js --mongo-url "mongodb://127.0.0.1:27017"
```

**Node.js Path Issues:**

```powershell
# Ensure you're in the correct directory
cd path\to\data-loader
npm install
node --version  # Should be 16+ for ES modules
```

**Execution Policy:**

```powershell
# If scripts are blocked
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

#### 4. **Alternative Testing**

```powershell
# Test with minimal parameters
node app.js -u 3 -m 1 -d 0.1 -a 1.0 --debug

# Test connection only
node -e "import('mongodb').then(m => m.MongoClient.connect('mongodb://localhost:27017').then(() => console.log('OK')))"
```

### Environment Variables

Create a `.env` file if connection fails:

```
MONGO_URL=mongodb://127.0.0.1:27017
DATABASE_NAME=nexusSocial
```
