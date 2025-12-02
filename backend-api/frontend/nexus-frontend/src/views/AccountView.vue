<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'
import { ref, onMounted, watchEffect, computed, watch } from 'vue'
import type { UserProfile } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { user, friendCount } = storeToRefs(userStore)

const username = ref<string>('')
const password = ref<string>('')
const email = ref<string>('')
const firstName = ref<string>('')
const lastName = ref<string>('')
const phone = ref<string>('')
const bio = ref<string>('')
const avatarUrl = ref<string>('defaultAvatar.jpg')
const avatarFile = ref<File | File[]>() 

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
      router.push('/')
    }
  }
  await userStore.fetchFriendCount()
})

watch(avatarFile, (newFile) => {
  const file = Array.isArray(newFile) ? newFile[0] : newFile;
  
  if (file) {
    const reader = new FileReader();
    reader.addEventListener("load", () => {
      if (typeof reader.result === 'string') {
        avatarUrl.value = reader.result;
      }
    });
    reader.readAsDataURL(file);
  }
})

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
      avatar: avatarUrl.value,
    }
    
    if (password.value) {
        (updatedUserData as any).password = password.value; 
    }

    await userStore.updateUser(updatedUserData)

    if (avatarFile.value) {
      alert('Text updated! (Note: Avatar upload logic needs backend implementation)')
    } else {
      alert('Profile updated!')
    }

    password.value = ''
    avatarFile.value = undefined

  } catch (err: any) {
    error.value = `Failed to update profile: ${err.message}`
    alert(error.value)
  } finally {
    loading.value = false
  }
}

function logout() {
  userStore.logout()
  router.push('/')
}
</script>

<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center">
      <div class="account-form">
        <div class="d-flex flex-column align-center mb-4">
          <v-avatar
            size="100"
            color="grey-lighten-3"
            class="mb-3"
            style="border: 2px solid orangered"
          >
            <v-img
              :src="avatarUrl"
              alt="User avatar"
              cover
              @error="avatarUrl = 'defaultAvatar.jpg'"
            />
          </v-avatar>
          <v-file-input
            v-model="avatarFile"
            label="Change avatar"
            accept="image/*"
            variant="outlined"
            density="compact"
            prepend-icon="mdi-camera"
            hide-details
            style="max-width: 320px"
          />
        </div>

        <router-link
          to="/friends-list"
          class="friend-link"
          style="color: orangered;"
        >
          {{ friendCount }} {{ friendCountLabel }}
        </router-link>

        <router-link
          to="/friend-requests"
          class="friend-link d-block my-2"
          style="color: orangered;"
        >
          View Your Friend Requests
        </router-link>

        <v-text-field
          v-model="username"
          label="Username"
          variant="underlined"
          readonly
          class="mb-2"
        />

        <v-text-field
          v-model="password"
          label="New Password"
          variant="underlined"
          type="password"
          class="mb-2"
          placeholder="Leave blank to keep unchanged"
        />

        <v-text-field
          v-model="email"
          label="Email"
          variant="underlined"
          class="mb-2"
          type="email"
        />

        <v-text-field
          v-model="firstName"
          label="First Name"
          variant="underlined"
          class="mb-2"
        />

        <v-text-field
          v-model="lastName"
          label="Last Name"
          variant="underlined"
          class="mb-2"
        />

        <v-text-field
          v-model="phone"
          label="Phone Number"
          variant="underlined"
          class="mb-2"
        />

        <v-textarea
          v-model="bio"
          label="Bio"
          variant="outlined"
          rows="4"
          placeholder="Write your bio..."
          class="mt-4 mb-2"
        />

        <v-btn
          @click="updateUser"
          color="orangered"
          class="my-button"
          :loading="loading"
        >
          Update User!
        </v-btn>
      </div>
    </v-container>
  </div>
</template>

<style scoped>
.center-container {
  display: flex;
  flex-direction: column;
  align-items: center;    
  justify-content: center; 
  width: 100vw;
  background-color: white;
  margin: 0;
  padding: 0;
  text-align: center; 
}
.account-form {
    width: 100%;
    max-width: 500px;
}
.action-link, .friend-link {
    text-decoration: none;
    margin: 5px;
    font-weight: bold;
}
</style>