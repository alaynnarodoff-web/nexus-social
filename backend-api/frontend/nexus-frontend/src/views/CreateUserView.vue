<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center" style="max-width: 400px;">
      
      <img src="@/assets/thumbnail_IMG_2719.png" alt="Logo" class="logo-img mb-4" />
      
      <h2 class="text-h5 font-weight-bold mb-4">Create Account</h2>

      <div class="w-100">
        <v-text-field v-model="username" label="Username" variant="underlined" density="compact"></v-text-field>
        <v-text-field v-model="password" label="Password" type="password" variant="underlined" density="compact"></v-text-field>
        <v-text-field v-model="email" label="Email" type="email" variant="underlined" density="compact"></v-text-field>
        <v-text-field v-model="firstName" label="First Name" variant="underlined" density="compact"></v-text-field>
        <v-text-field v-model="lastName" label="Last Name" variant="underlined" density="compact"></v-text-field>

        <div class="d-flex flex-column align-center my-3">
            <v-avatar size="80" class="mb-2" style="border: 1px solid #ccc;">
                <v-img :src="avatarBase64 || ''" alt="Avatar Preview">
                    <template v-slot:placeholder>
                        <div class="d-flex align-center justify-center fill-height text-grey text-caption">
                            No Image
                        </div>
                    </template>
                </v-img>
            </v-avatar>
            <v-file-input
                v-model="avatarFile"
                label="Upload Avatar"
                accept="image/*"
                variant="outlined"
                density="compact"
                prepend-icon="mdi-camera"
                hide-details
                @update:model-value="handleAvatarChange"
            ></v-file-input>
        </div>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-4 w-100">
        {{ error }}
      </v-alert>

      <v-btn 
        color="orangered" 
        block 
        class="mb-3 text-white" 
        :loading="loading"
        @click="createUser"
      >
        Create Account
      </v-btn>

      <router-link to="/login" class="action-link" style="color: orangered;">
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