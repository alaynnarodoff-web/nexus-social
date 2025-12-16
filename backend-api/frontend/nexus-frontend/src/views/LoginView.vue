<template>
  <div class="center-container">
    <img src="@/assets/thumbnail_IMG_2719.png" alt="My Image" />

    
     <div class="loginUser">
<v-text-field
  v-model="username"
  label="Username"
  variant="underlined"
  name="username"
  type="username"
  style="width: 320px"
></v-text-field>
</div>

    
<div class="loginUser">
  <v-text-field
    v-model="password"
    label="Password"
    variant="underlined"
    name="password"
    type="password"
    style="width: 320px"
  ></v-text-field>
</div>

<div v-if="error" class="error-message">
  {{ error }}
</div>

<v-btn @click="login">Login</v-btn>

    <router-link to="/create-user">Create User Page</router-link>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'

const username = ref<string>('')
const password = ref<string>('')

const userStore = useUserStore()

const { error, user } = storeToRefs(userStore)

const router = useRouter()

const login = async () => {
  try {
    await userStore.login(username.value, password.value)

    if (user.value) {
      sessionStorage.setItem('loggedInUser', username.value)
      console.log('login succeeded for', username.value)
      router.push('/view-posts')
    } else {
      console.log('Login failed, user object is null. Error:', error.value)
    }
  } catch (err) {
    console.error('An unexpected error occurred during login:', err)
  }
}
</script>

<style scoped>
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;    
  justify-content: center; 
  width: 100vw;
  height: 100vh;
  background-color: white;
  margin: 0;
  padding: 0;
  text-align: center; 
}

.center-container img {
  width: 120px;
  height: auto;
  margin-bottom: 20px;
}


.loginUser {
  display: flex;
  flex-direction: column;
  align-items: center; 
  margin: 10px 0;
  font-family: Cambria, Cochin, Georgia, Times, 'Times New Roman', serif;
  font-size: 14px;
  width: 250px;
  color: #000;
}

label {
  color: #000;
  margin-bottom: 5px;
}

input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  color: #000;
  background-color: #fff;
  text-align: center; 
}

.my-button {
  background-color: orangered;
  color: white;
  border: none;
  padding: 10px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
  width: 250px;
}

.my-button:hover {
  background-color: #b33000;
}

a {
  margin-top: 10px;
  color: orangered;
  text-decoration: none;
  display: inline-block;
}

a:hover {
  text-decoration: underline;
}
</style>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  background-color: white;
}

.error-message {
  color: red;
  font-family: Cambria, Cochin, Georgia, Times, 'Times New Roman', serif;
  margin-bottom: 15px;
  width: 320px; 
  text-align: center;
}
</style>