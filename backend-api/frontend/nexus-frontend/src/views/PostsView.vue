<script setup lang="ts">
import { ref, onMounted, watch } from 'vue' 
import { useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { useUserStore } from '@/stores/user' 
import { storeToRefs } from 'pinia'
import FriendRecsView from './FriendRecsView.vue'

const postsStore = usePostsStore()
const userStore = useUserStore()
const router = useRouter()

const { posts, loading, error } = storeToRefs(postsStore)
const { user } = storeToRefs(userStore) 

const activeFeed = ref<'everyone' | 'friends' | 'fof'>('everyone')
const searchQuery = ref('') 
const selectedUser = ref<string | null>(null) 
const suggestions = ref<any[]>([])
const suggestionsLoading = ref(false)
const debounceTimer = ref<any>(null) 

const commentInputs = ref<Record<string, string>>({})

onMounted(async () => {
  await postsStore.getNewsFeed()
  if (!user.value) await userStore.fetchUser()
})


function toggleLike(post: any) {
  postsStore.toggleLike(post.id)
}

function toggleCommentSection(post: any) {
  post.showComments = !post.showComments
}

async function postComment(postId: string) {
  const text = commentInputs.value[postId]
  if (!text || !text.trim()) return

  await postsStore.addComment(postId, text)
  
  commentInputs.value[postId] = ''
}


watch(activeFeed, (newFeed) => {
  if (newFeed === 'everyone') postsStore.getNewsFeed()
  else if (newFeed === 'friends') postsStore.getFriendsFeed() 
  else if (newFeed === 'fof') postsStore.getFriendsOfFriendsFeed()
})

watch(searchQuery, (newQuery) => {
  suggestions.value = []
  if (debounceTimer.value) clearTimeout(debounceTimer.value)

  if (!newQuery || newQuery.length < 3) {
    suggestionsLoading.value = false
    return
  }
  suggestionsLoading.value = true
  debounceTimer.value = setTimeout(async () => {
    try {
      const response = await fetch(`/api/users/search?q=${newQuery}`)
      if (!response.ok) throw new Error('Failed to fetch users')
      suggestions.value = await response.json()
    } catch (err) {
      console.error(err)
      suggestions.value = []
    } finally {
      suggestionsLoading.value = false
    }
  }, 300)
})

watch(selectedUser, (selectedUsername) => {
  if (selectedUsername) {
    router.push(`/profile/${selectedUsername}`)
    selectedUser.value = null
    searchQuery.value = ''
    suggestions.value = []
  }
})
</script>

<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center">
      
      <div class="feed-controls">
        <v-autocomplete
          v-model="selectedUser"
          v-model:search="searchQuery"
          :items="suggestions"
          :loading="suggestionsLoading"
          item-title="username"
          item-value="username"
          no-filter
          label="Search for users..."
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          density="comfortable"
          class="mb-4 search-field"
          hide-details
          color="orangered"
          :return-object="false"
        >
           <template v-slot:item="{ props, item }">
            <v-list-item v-bind="props" :title="item.raw.username" :subtitle="`${item.raw.firstName} ${item.raw.lastName}`"></v-list-item>
          </template>
        </v-autocomplete>

        <v-btn-toggle
          v-model="activeFeed"
          divided
          color="orangered"
          variant="outlined"
          mandatory
          class="mb-6 w-100 justify-center"
        >
          <v-btn value="everyone" class="flex-grow-1">Everyone</v-btn>
          <v-btn value="friends" class="flex-grow-1">Friends</v-btn>
          <v-btn value="fof" class="flex-grow-1">Friends of Friends</v-btn>
        </v-btn-toggle>
      </div>
      <div>
        <v-row>
          <v-col class="text-left text-h5 font-weight-bold mb-2" style="color: orangered;">
          People you may know:
          </v-col>
          </v-row>
          <FriendRecsView />
        
      </div>

      <div class="feed-content">
        <div v-if="loading" class="text-center">
          <v-progress-circular indeterminate color="orangered" size="64" />
          <p class="mt-4">Loading feed...</p>
        </div>

        <v-alert v-else-if="error" type="error" variant="tonal" class="text-left">{{ error }}</v-alert>
        <v-alert v-else-if="!posts || posts.length === 0" type="info" variant="tonal" color="orange-darken-2" class="text-left">No posts to show.</v-alert>

        <div v-else>
          <v-card
            v-for="post in posts"
            :key="post.id"
            class="mb-5 mx-auto post-card"
            variant="outlined"
          >
            <v-list-item class="py-3">
              <template v-slot:prepend>
                <v-avatar size="52" class="mr-3" style="border: 1px solid orangered">
                  <v-img
                    v-if="post.authorAvatar"
                    :src="post.authorAvatar"
                    cover
                  />
                  <span v-else class="text-h6">{{ post.authorId?.charAt(0).toUpperCase() }}</span>
                </v-avatar>
              </template>
              <v-list-item-title class="font-weight-bold">{{ post.authorId }}</v-list-item-title>
              <v-list-item-subtitle>{{ new Date(post.timestamp).toLocaleString() }}</v-list-item-subtitle>
            </v-list-item>

            <v-divider />

            <v-card-text class="text-body-1 py-4 text-left" style="color: black">
              {{ post.content }}
            </v-card-text>

            <v-img
               v-if="post.imageUrl || post.imageBase64"
               :src="post.imageUrl || `data:image/jpeg;base64,${post.imageBase64}`"
               max-height="400"
               cover
               class="bg-grey-lighten-2"
             ></v-img>

            <v-divider />

            <v-card-actions>
              <v-btn 
                variant="text" 
                color="orangered"
                @click="toggleLike(post)"
              >
                <template v-slot:prepend>
                    <v-icon :icon="post.likedBy.includes(user?.username || '') ? 'mdi-heart' : 'mdi-heart-outline'"></v-icon>
                </template>
                {{ post.likedBy?.length || 0 }} Likes
              </v-btn>
              
              <v-btn 
                variant="text" 
                prepend-icon="mdi-comment-text-outline" 
                color="orangered"
                @click="toggleCommentSection(post)"
              >
                {{ post.comments?.length || 0 }} Comments
              </v-btn>
              <v-spacer />
            </v-card-actions>

            <div v-if="post.showComments" class="bg-grey-lighten-5 pa-3" style="border-top: 1px solid #eee">
                
                <div v-if="post.comments && post.comments.length > 0" class="mb-3">
                    <div v-for="(comment, index) in post.comments" :key="index" class="d-flex align-start mb-3 text-left">
                        <v-avatar size="32" class="mr-2 mt-1" style="border: 1px solid #ccc">
                            <v-img :src="comment.authorAvatar || 'defaultAvatar.jpg'"></v-img>
                        </v-avatar>
                        <div class="bg-white pa-2 rounded elevation-1 flex-grow-1">
                            <div class="text-subtitle-2 font-weight-bold">{{ comment.author }}</div>
                            <div class="text-body-2">{{ comment.text }}</div>
                            <div class="text-caption text-grey mt-1">{{ new Date(comment.timestamp).toLocaleString() }}</div>
                        </div>
                    </div>
                </div>

                <div class="d-flex align-center">
                    <v-text-field
                        v-model="commentInputs[post.id]"
                        placeholder="Write a comment..."
                        variant="outlined"
                        density="compact"
                        hide-details
                        bg-color="white"
                        class="mr-2"
                        @keyup.enter="postComment(post.id)"
                    ></v-text-field>
                    <v-btn 
                        color="orangered" 
                        size="small" 
                        @click="postComment(post.id)"
                        :disabled="!commentInputs[post.id]"
                    >
                        Post
                    </v-btn>
                </div>
            </div>

          </v-card>
        </div>
      </div>
    </v-container>
  </div>
</template>

<style scoped>
.feed-controls, .feed-content, .post-card {
  width: 100%;
  max-width: 700px;
}
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;    
  justify-content: center; 
  width: 100vw;
  background-color: white;
  margin: 0;
  padding: 0;
  text-align: center; 
}
</style>