<template>
  <div class="center-container">
    <v-container class="fill-height d-flex flex-column align-center justify-center">
      
      <div class="login-wrapper d-flex flex-column align-center w-100">
        
        <img src="@/assets/thumbnail_IMG_2719.png" alt="Nexus Social Logo" class="logo-img mb-8" />

        <div class="input-group w-100 d-flex flex-column align-center">
          <v-text-field
            v-model="username"
            label="Username"
            variant="underlined"
            color="orangered"
            class="mb-2 w-100 custom-field"
          ></v-text-field>

          <v-text-field
            v-model="password"
            label="Password"
            variant="underlined"
            color="orangered"
            type="password"
            class="mb-6 w-100 custom-field"
          ></v-text-field>

          <v-alert 
            v-if="localError" 
            type="error" 
            variant="tonal" 
            class="mb-6 w-100 custom-field"
          >
            {{ localError }}
          </v-alert>

          <v-btn 
            block 
            elevation="4"
            height="54"
            class="mb-6 text-white font-weight-bold custom-field"
            @click="login"
            style="background-color: orangered !important;"
          >
            LOGIN
          </v-btn>

          <router-link to="/create-user" class="create-account-link">
            Create an Account
          </router-link>
        </div>
      </div>

    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'

const username = ref<string>('')
const password = ref<string>('')
const localError = ref<string | null>(null) 

const userStore = useUserStore()
const { user } = storeToRefs(userStore)
const router = useRouter()

const login = async () => {
  localError.value = null
  userStore.error = null

  if (!username.value.trim() || !password.value.trim()) {
    localError.value = "Please enter both your username and password."
    return
  }

  try {
    await userStore.login(username.value, password.value)

    if (user.value) {
      sessionStorage.setItem('loggedInUser', username.value)
      router.push('/view-posts')
    } else {
      localError.value = "Username or password is incorrect."
    }
  } catch (err: any) {
    const msg = err.message || ""
    if (msg.includes('401') || err.status === 401 || msg.toLowerCase().includes('unauthorized')) {
      localError.value = "Username or password is incorrect."
    } else {
      localError.value = "The login service is currently unavailable. Please try again later."
    }
  }
}
</script>

<style scoped>
.center-container {
  width: 100vw;
  min-height: 100vh;
  background-color: white;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-wrapper {
  max-width: 400px;
}

.logo-img {
  width: 150px;
  height: auto;
}

.custom-field {
  max-width: 320px;
}

.create-account-link {
  color: orangered;
  text-decoration: none;
  font-weight: bold;
  font-size: 1rem;
}

.create-account-link:hover {
  text-decoration: underline;
}

:deep(.v-alert) {
  height: auto !important;
  text-align: left;
}
</style>