<template>
  <div class="center-container">
    <div class="header-area">
      <button @click="$router.push('/topics')" class="back-btn">← Back to Cloud</button>
      <h2>Topic: <span class="highlight">#{{ topic }}</span></h2>
    </div>

    <div v-if="loading" class="mt-4">
      <v-progress-circular indeterminate color="orangered" size="40"></v-progress-circular>
      <div class="mt-2 text-grey">Loading posts...</div>
    </div>

    <div v-else-if="posts.length === 0" class="no-posts">
      No posts found for this topic.
    </div>

    <div v-else class="w-100" style="max-width: 700px;">
        <v-card
            v-for="post in posts"
            :key="post.id"
            class="mb-5 mx-auto post-card"
            variant="outlined"
        >
            <v-list-item class="py-3 text-left">
                <template v-slot:prepend>
                    <v-avatar 
                      size="52" 
                      class="mr-3 hover-avatar"
                      @click="goToProfile(post.authorId)"
                    >
                        <v-img
                            v-if="post.authorAvatar"
                            :src="post.authorAvatar"
                            cover
                        />
                        <span v-else class="text-h6">{{ post.authorId?.charAt(0).toUpperCase() }}</span>
                    </v-avatar>
                </template>
                <v-list-item-title 
                  class="font-weight-bold author-link" 
                  @click="goToProfile(post.authorId)"
                >
                  {{ post.authorId }}
                </v-list-item-title>
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
                        <v-icon :icon="post.likedBy?.includes(user?.username || '') ? 'mdi-heart' : 'mdi-heart-outline'"></v-icon>
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
            </v-card-actions>

            <div v-if="post.showComments" class="bg-grey-lighten-5 pa-3" style="border-top: 1px solid #eee">
                <div v-if="post.comments && post.comments.length > 0" class="mb-3">
                    <div v-for="(comment, index) in post.comments" :key="index" class="d-flex align-start mb-3 text-left">
                        <v-avatar 
                          size="32" 
                          class="mr-2 mt-1 hover-avatar" 
                          @click="goToProfile(comment.author)"
                        >
                            <v-img :src="comment.authorAvatar || 'defaultAvatar.jpg'"></v-img>
                        </v-avatar>
                        <div class="bg-white pa-2 rounded elevation-1 flex-grow-1">
                            <div 
                              class="text-subtitle-2 font-weight-bold author-link" 
                              @click="goToProfile(comment.author)"
                            >
                              {{ comment.author }}
                            </div>
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
</template>

<script setup lang="ts">
import { usePostsStore } from '@/stores/posts'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { onMounted, watch, ref } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps<{ topic: string }>()

const router = useRouter()
const postStore = usePostsStore()
const userStore = useUserStore()
const { posts, loading } = storeToRefs(postStore)
const { user } = storeToRefs(userStore)

const commentInputs = ref<Record<string, string>>({})

onMounted(async () => {
    if (!user.value) await userStore.fetchUser()
    postStore.fetchPostsByTopic(props.topic)
})

watch(() => props.topic, (newTopic) => {
    postStore.fetchPostsByTopic(newTopic)
})

function goToProfile(username: string) {
  if (username) router.push(`/profile/${username}`)
}

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
</script>

<style scoped>
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100vw;
  min-height: 100vh;
  background-color: white; 
  padding: 40px 20px;
}

.header-area {
  margin-bottom: 30px;
  text-align: center;
}

.highlight {
  color: #E65100;
}

.back-btn {
  background: none;
  border: 1px solid #ccc;
  padding: 5px 10px;
  cursor: pointer;
  margin-bottom: 10px;
  border-radius: 4px;
  color: #666;
  font-size: 0.9rem;
}

.back-btn:hover {
    color: #E65100;
    border-color: #E65100;
}

.post-card {
  width: 100%;
  background: white;
}

.no-posts {
    color: #888;
    font-style: italic;
    margin-top: 20px;
}

.hover-avatar {
  border: 1px solid orangered;
  cursor: pointer;
  transition: all 0.2s ease;
}
.hover-avatar:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.author-link {
  cursor: pointer;
  transition: color 0.2s ease;
}
.author-link:hover {
  color: #E65100;
  text-decoration: underline;
}
</style>