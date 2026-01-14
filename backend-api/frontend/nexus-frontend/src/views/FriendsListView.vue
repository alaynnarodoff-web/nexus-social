<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center">
      <h2 class="text-h4 font-weight-bold mb-6" style="color: #333;">Your Friends</h2>

      <div v-if="loading" class="my-4">
        <v-progress-circular indeterminate color="orangered" size="50"></v-progress-circular>
      </div>

      <v-alert v-else-if="error" type="error" variant="tonal" class="mb-4">
        {{ error }}
      </v-alert>

      <div v-else-if="friends.length === 0">
        <p class="text-h6 text-grey">You haven't added any friends yet.</p>
      </div>

      <div v-else class="w-100 d-flex flex-column align-center">
        <v-card
          v-for="friend in friends"
          :key="friend.username"
          class="friend-card mb-3 elevation-1"
          width="100%"
          max-width="400"
          :to="`/profile/${friend.username}`"
        >
          <v-card-text class="d-flex align-center">
            <v-avatar 
              size="52" 
              class="mr-3"
              color="grey-lighten-3"
              style="border: 1px solid #ffd6b3"
            >
              <v-img
                v-if="friend.avatar"
                :src="friend.avatar"
                alt="Friend avatar"
                cover
              />
              <span v-else class="text-h6 font-weight-bold" style="color: orangered;">
                {{ friend.username.charAt(0).toUpperCase() }}
              </span>
            </v-avatar>

            <div class="text-left">
              <div class="text-subtitle-1 font-weight-bold" style="color: #333;">
                {{ friend.username }}
              </div>
              <div class="text-caption text-grey">
                {{ friend.firstName }} {{ friend.lastName }}
              </div>
            </div>

            <v-spacer></v-spacer>
            <v-icon color="grey">mdi-chevron-right</v-icon>
          </v-card-text>
        </v-card>
      </div>

      <router-link to="/account" class="mt-6" style="color: orangered; text-decoration: none; font-weight: bold;">
        Back to Account
      </router-link>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, type UserProfile } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { user } = storeToRefs(userStore)

// Change from string[] to UserProfile[]
const friends = ref<UserProfile[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    if (!user.value) {
        await userStore.fetchUser()
        if (!user.value) throw new Error("Not logged in")
    }
    const currentUsername = user.value?.username

    const res = await fetch('/api/friends', { credentials: 'include' })
    
    if (res.status === 401) {
        router.push('/login')
        return
    }

    if (!res.ok) throw new Error("Failed to load friends list")
    
    const friendships = await res.json()
    const friendSet = new Set<string>()
    
    // 1. Get the usernames from the friend requests
    friendships.forEach((f: any) => {
        if (f.requestingUserId === currentUsername) {
            friendSet.add(f.requestRecipientId)
        } else {
            friendSet.add(f.requestingUserId)
        }
    })

    const usernames = Array.from(friendSet)

    // 2. NEW: Fetch the full profiles for these usernames
    // We map each username to a fetch call
    const profilePromises = usernames.map(name => userStore.fetchUserByUsername(name))
    
    // Resolve all promises and filter out any null results
    const profiles = await Promise.all(profilePromises)
    friends.value = profiles.filter((p): p is UserProfile => p !== null)

  } catch (err: any) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;    
  width: 100vw;
  min-height: 100vh; 
  background-color: white;
  padding-top: 20px;
}

.friend-card {
    background-color: #fff3e6 !important;
    border: 1px solid #ffd6b3;
    transition: transform 0.2s;
    text-decoration: none;
}

.friend-card:hover {
    transform: translateY(-2px);
}
</style>