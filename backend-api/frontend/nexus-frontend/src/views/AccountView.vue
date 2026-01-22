<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center">
      <div class="account-form">
        
        <div class="d-flex flex-column align-center mb-6">
          <v-avatar
            size="120"
            color="grey-lighten-4"
            class="mb-3 elevation-3"
            style="border: 3px solid orangered"
          >
            <v-img
              :src="avatarBase64 || avatarUrl || 'defaultAvatar.jpg'"
              alt="User avatar"
              cover
            >
              <template v-slot:placeholder>
                <div class="d-flex align-center justify-center fill-height bg-grey-lighten-4">
                  <v-icon color="grey-lighten-1" size="40">mdi-account</v-icon>
                </div>
              </template>
            </v-img>
          </v-avatar>

          <v-btn
            v-if="avatarBase64"
            variant="text"
            color="grey-darken-1"
            size="small"
            class="mb-2"
            prepend-icon="mdi-close"
            @click="clearAvatarSelection"
          >
            Cancel Selection
          </v-btn>

          <div 
            class="d-flex align-center justify-center upload-trigger-btn pa-2" 
            @click="triggerFileInput"
            v-ripple
          >
            <v-icon color="orangered" icon="mdi-camera" class="mr-2"></v-icon>
            <span class="text-body-2 font-weight-bold" style="color: orangered;">
              {{ avatarBase64 ? 'Change Selection' : 'Change Profile Photo' }}
            </span>
          </div>

          <v-file-input
            ref="fileInputRef"
            v-model="avatarFile"
            accept="image/*"
            hide-details
            class="d-none"
            @update:model-value="handleAvatarChange"
          ></v-file-input>
        </div>

        <div class="mb-6">
          <router-link to="/friends-list" class="friend-link" style="color: orangered;">
            {{ friendCount }} {{ friendCountLabel }}
          </router-link>
          <router-link to="/friend-requests" class="friend-link d-block mt-2" style="color: orangered;">
            View Your Friend Requests
          </router-link>
        </div>

        <v-text-field
          v-model="username"
          label="Username"
          variant="underlined"
          readonly
          class="mb-2"
          color="orangered"
        />

        <v-text-field
          v-model="email"
          label="Email"
          variant="underlined"
          class="mb-2"
          type="email"
          color="orangered"
        />

        <v-text-field
          v-model="firstName"
          label="First Name"
          variant="underlined"
          class="mb-2"
          color="orangered"
        />

        <v-text-field
          v-model="lastName"
          label="Last Name"
          variant="underlined"
          class="mb-2"
          color="orangered"
        />

        <v-text-field
          v-model="phone"
          label="Phone Number"
          variant="underlined"
          class="mb-2"
          color="orangered"
        />

        <v-textarea
          v-model="bio"
          label="Bio"
          variant="outlined"
          rows="4"
          placeholder="Write your bio..."
          class="mt-4 mb-6"
          color="orangered"
        />

        <v-btn
          block
          size="large"
          elevation="4"
          height="54"
          class="mb-4"
          :loading="loading"
          @click="updateUser"
          style="background-color: orangered !important; color: white !important;"
        >
          <span style="color: white !important; font-weight: bold; font-size: 1.1rem;">
            UPDATE PROFILE
          </span>
        </v-btn>

        <v-btn
          block
          variant="text"
          color="grey-darken-1"
          @click="logout"
        >
          Logout
        </v-btn>
      </div>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { ref, onMounted, watchEffect, computed } from 'vue'
import type { UserProfile } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { user, friendCount } = storeToRefs(userStore)

const username = ref<string>('')
const email = ref<string>('')
const firstName = ref<string>('')
const lastName = ref<string>('')
const phone = ref<string>('')
const bio = ref<string>('')
const avatarUrl = ref<string>('defaultAvatar.jpg')

const fileInputRef = ref<any>(null)
const avatarFile = ref<File | File[]>() 
const avatarBase64 = ref<string>('')

const loading = ref<boolean>(false)
const error = ref<string | null>(null)

watchEffect(() => {
  if (user.value) {
    username.value = user.value.username
    email.value = user.value.email
    firstName.value = user.value.firstName
    lastName.value = user.value.lastName
    phone.value = user.value.phone
    bio.value = user.value.bio
    avatarUrl.value = user.value.avatar || 'defaultAvatar.jpg'
  }
})

const friendCountLabel = computed(() => {
  return friendCount.value === 1 ? 'friend' : 'friends'
})

onMounted(async () => {
  if (!user.value) {
    try {
      await userStore.fetchUser()
    } catch (err) {
      router.push('/login')
    }
  }
  await userStore.fetchFriendCount()
})

function triggerFileInput() {
    fileInputRef.value?.click()
}

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
    }
}

function clearAvatarSelection() {
    avatarFile.value = undefined
    avatarBase64.value = ''
}

async function updateUser() {
  loading.value = true
  error.value = null

  try {
    const updatedUserData: Partial<UserProfile> = {
      username: username.value,
      email: email.value,
      firstName: firstName.value,
      lastName: lastName.value,
      phone: phone.value,
      bio: bio.value,
      avatar: avatarBase64.value || avatarUrl.value,
    }

    await userStore.updateUser(updatedUserData)
    alert('Profile updated successfully!')
    
    avatarBase64.value = '' 
    avatarFile.value = undefined

  } catch (err: any) {
    alert(`Failed to update profile: ${err.message}`)
  } finally {
    loading.value = false
  }
}

function logout() {
  userStore.logout()
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
  padding: 40px 0;
}
.account-form {
    width: 100%;
    max-width: 500px;
}
.friend-link {
    text-decoration: none;
    font-weight: bold;
    font-size: 1.1rem;
}

.upload-trigger-btn {
    cursor: pointer;
    border-radius: 8px;
    transition: background-color 0.2s ease;
    display: inline-flex;
}

.upload-trigger-btn:hover {
    background-color: #fff3e6;
}
</style>