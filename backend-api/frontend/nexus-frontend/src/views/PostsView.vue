<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { usePostsStore } from '@/stores/posts'
import { storeToRefs } from 'pinia'
const postsStore = usePostsStore()
const { posts, loading, error } = storeToRefs(postsStore)
const activeFeed = ref<'everyone' | 'friends' | 'fof'>('everyone')

const searchQuery = ref('')
const suggestions = ref<{ username: string }[]>([])

const feedOptions = {
  everyone: 'Everyone',
  friends: 'Friends',
  fof: 'Friends of Friends',
}

const router = useRouter()


onMounted(async () => {
  await postsStore.getNewsFeed()
})
</script>
<template>
  <v-container>
    <v-card
      v-for="post in posts"
      :key="post.id"
      class="mb-4"
      style="color: black"
    >
      <v-card-text>
        {{ post.content }}
      </v-card-text>
    </v-card>
  </v-container>
</template>