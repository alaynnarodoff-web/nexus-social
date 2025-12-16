<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const showNavBar = computed(() => {
  const hiddenRoutes = ['/login', '/create-user', '/']
  return !hiddenRoutes.includes(route.path)
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <v-app>
    <v-main>
      <RouterView />
    </v-main>

    <v-bottom-navigation
      v-if="showNavBar"
      color="orangered"
      grow
      app
    >
      <v-btn to="/view-posts">
        <v-icon>mdi-home-variant</v-icon>
        <span>Feed</span>
      </v-btn>

      <v-btn to="/create-post">
        <v-icon>mdi-plus-box</v-icon>
        <span>Create</span>
      </v-btn>

      <v-btn to="/account">
        <v-icon>mdi-account</v-icon>
        <span>Account</span>
      </v-btn>

      <v-btn @click="handleLogout">
        <v-icon>mdi-logout</v-icon>
        <span>Sign Out</span>
      </v-btn>
    </v-bottom-navigation>
  </v-app>
</template>

<style>
:root { box-sizing: border-box; }
#app { margin: 0; height: 100%; }

.v-main {
  padding-bottom: 56px; 
}
</style>