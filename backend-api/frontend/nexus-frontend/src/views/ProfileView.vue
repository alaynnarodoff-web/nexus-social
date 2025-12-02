<template>
  <div class="center-container">
    <v-container v-if="loadingProfile" class="d-flex justify-center align-center" style="height: 50vh;">
      <v-progress-circular indeterminate color="orangered"></v-progress-circular>
    </v-container>

    <v-container v-else-if="error" class="text-center">
      <h3 class="text-red">{{ error }}</h3>
      <v-btn to="/" variant="text" color="orangered">Go Home</v-btn>
    </v-container>

    <v-container v-else-if="profileUser" class="d-flex flex-column align-center text-center">
      <div class="content-wrapper">
        
        <div class="d-flex flex-column align-center mb-6">
          <v-avatar
            size="120"
            color="grey-lighten-3"
            class="mb-3 elevation-3"
            style="border: 3px solid orangered"
          >
            <v-img
              :src="profileUser.avatar || 'defaultAvatar.jpg'"
              alt="User avatar"
              cover
              @error="profileUser!.avatar = 'defaultAvatar.jpg'"
            />
          </v-avatar>
          
          <h2 class="text-h4 font-weight-bold text-grey-darken-3">{{ profileUser.username }}</h2>
          <p class="text-subtitle-1 text-grey">{{ profileUser.firstName }} {{ profileUser.lastName }}</p>
          
          <v-chip class="mt-2" color="orangered" variant="outlined" size="small">
            {{ profileUser.friendCount || 0 }} Friends
          </v-chip>
        </div>

        <v-card variant="tonal" class="pa-4 mb-6 text-left bg-grey-lighten-4 border-none">
          <div class="text-caption font-weight-bold text-grey-darken-1 mb-1">About</div>
          <div class="text-body-1 text-grey-darken-3">
            {{ profileUser.bio || 'This user has not written a bio yet.' }}
          </div>
        </v-card>

        <div class="d-flex justify-center gap-2 mb-8">
           <router-link 
             v-if="isCurrentUser" 
             to="/account" 
             class="v-btn v-btn--elevated bg-orangered text-white px-6"
             style="text-decoration: none; height: 36px; line-height: 36px; border-radius: 4px;"
            >
             Edit Profile
           </router-link>

           <v-btn 
             v-else 
             variant="flat" 
             color="orangered" 
             prepend-icon="mdi-account-plus"
             @click="sendFriendRequest"
           >
             Add Friend
           </v-btn>
        </div>

        <v-divider class="mb-6"></v-divider>

        <h3 class="text-h5 font-weight-bold mb-4 text-left align-self-start w-100">
          Posts by {{ profileUser.username }}
        </h3>

        <div v-if="loadingPosts" class="my-4">
           <v-progress-circular indeterminate color="orangered" size="30"></v-progress-circular>
        </div>

        <div v-else-if="posts.length === 0" class="text-grey font-italic my-4">
           No posts to show.
        </div>

        <div v-else class="w-100">
          <v-card
            v-for="post in posts"
            :key="post.id"
            class="mb-5 mx-auto post-card text-left"
            variant="outlined"
          >
            <v-list-item class="py-3">
              <template v-slot:prepend>
                <v-avatar size="52" class="mr-3">
                  <v-img
                    v-if="post.authorAvatar"
                    :src="post.authorAvatar"
                    alt="Author avatar"
                    cover
                  />
                  <span v-else class="text-h6">
                    {{ post.authorId?.charAt(0).toUpperCase() }}
                  </span>
                </v-avatar>
              </template>

              <v-list-item-title class="font-weight-bold">
                {{ post.authorId || 'Anonymous' }}
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ new Date(post.timestamp).toLocaleString() }}
              </v-list-item-subtitle>

              <template v-slot:append>
                <v-btn icon="mdi-dots-vertical" variant="text" size="small" />
              </template>
            </v-list-item>

            <v-divider />

            <v-card-text class="text-body-1 py-4" style="color: black">
              {{ post.content }}
            </v-card-text>

            <v-img
               v-if="post.imageUrl || post.imageBase64"
               :src="post.imageUrl || `data:image/jpeg;base64,${post.imageBase64}`"
               max-height="300"
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
                    <v-icon 
                      :icon="post.likedBy?.includes(loggedInUser?.username || '') ? 'mdi-heart' : 'mdi-heart-outline'"
                    ></v-icon>
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

<script setup lang="ts">
import { useUserStore, type UserProfile } from '@/stores/user'
import { usePostsStore } from '@/stores/posts'
import { storeToRefs } from 'pinia'
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const userStore = useUserStore()
const postStore = usePostsStore()

const { user: loggedInUser } = storeToRefs(userStore)
const { posts } = storeToRefs(postStore)

const profileUser = ref<(UserProfile & { friendCount?: number }) | null>(null)
const loadingProfile = ref<boolean>(true)
const loadingPosts = ref<boolean>(false)
const error = ref<string | null>(null)
const commentInputs = ref<Record<string, string>>({})

const props = defineProps<{
  username: string
}>()

const isCurrentUser = computed(() => {
  return loggedInUser.value?.username === props.username
})

function toggleLike(post: any) {
  postStore.toggleLike(post.id)
}

function toggleCommentSection(post: any) {
  post.showComments = !post.showComments
}

async function postComment(postId: string) {
  const text = commentInputs.value[postId]
  if (!text || !text.trim()) return
  await postStore.addComment(postId, text)
  commentInputs.value[postId] = ''
}

const loadData = async (usernameToFetch: string) => {
  loadingProfile.value = true
  loadingPosts.value = true
  error.value = null
  profileUser.value = null
  
  try {
    if (!loggedInUser.value) {
        await userStore.fetchUser()
    }

    if (loggedInUser.value?.username === usernameToFetch) {
        profileUser.value = { ...loggedInUser.value!, friendCount: userStore.friendCount }
    } else {
        const data = await userStore.fetchUserByUsername(usernameToFetch)
        if (!data) throw new Error('User not found')
        profileUser.value = data
    }

    await postStore.fetchUserPosts(usernameToFetch)

  } catch (err: any) {
    error.value = err.message || 'Could not load profile'
  } finally {
    loadingProfile.value = false
    loadingPosts.value = false
  }
}

watch(() => props.username, (newUsername) => {
  if (newUsername) loadData(newUsername)
})

onMounted(() => {
  loadData(props.username)
})

function sendFriendRequest() {
    alert(`Friend request sent to ${props.username}!`)
}
</script>

<style scoped>
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;    
  width: 100vw;
  min-height: 100vh; 
  background-color: white;
  margin: 0;
  padding: 0;
}

.content-wrapper {
    width: 100%;
    max-width: 700px;
}

.post-card {
  width: 100%;
  max-width: 700px;
}
</style>