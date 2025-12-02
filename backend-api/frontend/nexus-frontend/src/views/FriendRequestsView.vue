<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center">
      <h2 class="text-h4 font-weight-bold mb-6" style="color: #333;">Your Friend Requests</h2>

      <div v-if="loading" class="my-4">
        <v-progress-circular indeterminate color="orangered" size="50"></v-progress-circular>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable>
        {{ error }}
      </v-alert>

      <div v-if="!loading && requests.length === 0">
        <p class="text-h6 text-grey">No friend requests right now.</p>
      </div>

      <div v-else class="w-100 d-flex flex-column align-center">
        <v-card
          v-for="req in requests"
          :key="req.id || req.requestingUserId"
          class="request-card mb-4 elevation-2"
          width="100%"
          max-width="400"
        >
          <v-card-text class="text-body-1 text-left" style="color: #333;">
            <span class="font-weight-bold">{{ req.requestingUserId }}</span> sent you a friend request.
          </v-card-text>

          <v-card-actions class="justify-center pb-3">
            <v-btn
              color="success"
              variant="elevated"
              prepend-icon="mdi-check"
              class="mr-2"
              @click="respond(req.requestingUserId, true)"
            >
              Accept
            </v-btn>

            <v-btn
              color="error"
              variant="elevated"
              prepend-icon="mdi-close"
              @click="respond(req.requestingUserId, false)"
            >
              Reject
            </v-btn>
          </v-card-actions>
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
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const requests = ref<any[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const res = await fetch("/api/friends/requests", { credentials: 'include' })
    
    if (res.status === 401) {
      router.push('/login')
      return
    }

    if (!res.ok) throw new Error("Failed to load requests")
    requests.value = await res.json()
  } catch (err: any) {
    error.value = err.message
  } finally {
    loading.value = false
  }
})

async function respond(fromUser: string, accept: boolean) {
  try {
    const res = await fetch(`/api/friends/respond?fromUser=${fromUser}&accept=${accept}`, {
      method: "POST",
      credentials: 'include'
    })

    if (res.status === 401) {
      router.push('/login')
      return
    }
    
    const msg = await res.text()
    alert(msg)

    requests.value = requests.value.filter(r => r.requestingUserId !== fromUser)
    
    if (accept) {
        await userStore.fetchFriendCount()
    }

  } catch (err) {
    alert("Error responding to request.")
    console.error(err)
  }
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
  padding-top: 20px;
}

.request-card {
    background-color: #fff3e6 !important;
    border: 1px solid #ffd6b3;
}
</style>