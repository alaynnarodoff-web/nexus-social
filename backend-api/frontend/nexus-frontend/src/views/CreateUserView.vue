<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center" style="max-width: 400px;">
      <img src="@/assets/thumbnail_IMG_2719.png" alt="Logo" class="logo-img mb-4" />
      <h2 class="text-h5 font-weight-bold mb-4">Create Account</h2>

      <div class="w-100">
        <v-text-field v-model="username" label="Username" variant="underlined" color="orangered"></v-text-field>
        <v-text-field v-model="password" label="Password" type="password" variant="underlined" color="orangered"></v-text-field>
        <v-text-field v-model="email" label="Email" type="email" variant="underlined" color="orangered"></v-text-field>
        <v-text-field v-model="phoneNumber" label="Phone Number" variant="underlined" color="orangered"></v-text-field>
        <v-text-field v-model="firstName" label="First Name" variant="underlined" color="orangered"></v-text-field>
        <v-text-field v-model="lastName" label="Last Name" variant="underlined" color="orangered"></v-text-field>
        <v-textarea
          v-model="bio"
          label="Add a Bio!"
          variant="outlined"
          rows="3"
          class="mt-2"
          color="orangered"
        ></v-textarea>

        <div class="d-flex flex-column align-center my-6">
            <v-avatar size="100" class="mb-3 elevation-2" style="border: 2px solid orangered;">
                <v-img :src="avatarBase64 || ''" cover>
                    <template v-slot:placeholder>
                        <div class="d-flex align-center justify-center fill-height bg-grey-lighten-4">
                            <v-icon color="grey-lighten-1" size="40">mdi-account</v-icon>
                        </div>
                    </template>
                </v-img>
            </v-avatar>
            
            <v-file-input
                v-model="avatarFile"
                label="Choose Avatar"
                accept="image/*"
                variant="outlined"
                density="compact"
                prepend-icon="mdi-camera"
                hide-details
                color="orangered"
                style="width: 250px"
                @update:model-value="handleAvatarChange"
            ></v-file-input>
        </div>
      </div>

      <v-btn 
        color="orangered" 
        block 
        variant="flat"
        class="mb-3 text-white font-weight-bold" 
        size="large"
        :loading="loading"
        @click="createUser"
      >
        Create Account
      </v-btn>

      <router-link to="/login" class="text-decoration-none font-weight-bold" style="color: orangered;">
        Back to Login
      </router-link>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')
const email = ref('')
const firstName = ref('')
const lastName = ref('')
const phoneNumber = ref('') 
const bio = ref('')

const avatarFile = ref<File[] | undefined>()
const avatarBase64 = ref<string>('')
const loading = ref(false)
const error = ref<string | null>(null)

function handleAvatarChange(files: File | File[]) {
    const file = Array.isArray(files) ? files[0] : files;
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            if (typeof e.target?.result === 'string') {
                avatarBase64.value = e.target.result;
            }
        }
        reader.readAsDataURL(file);
    } else {
        avatarBase64.value = '';
    }
}

async function createUser() {
    loading.value = true
    error.value = null

    try {
        const response = await fetch('/api/user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username.value,
                password: password.value,
                email: email.value,
                firstName: firstName.value,
                lastName: lastName.value,
                phoneNumber: phoneNumber.value,
                bio: bio.value,
                avatar: avatarBase64.value 
            })
        })

        if (response.ok) {
            alert('Account created successfully!')
            router.push('/login')
        } else {
            const text = await response.text()
            throw new Error(text || "Failed to create account")
        }
    } catch (err: any) {
        console.error(err)
        error.value = err.message
    } finally {
        loading.value = false
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
.logo-img {
    width: 100px;
    height: auto;
}
.action-link {
    text-decoration: none;
    font-weight: bold;
}
</style>