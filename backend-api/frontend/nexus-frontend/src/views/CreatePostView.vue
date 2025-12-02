<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center" style="max-width: 500px;">
      
      <img src="@/assets/thumbnail_IMG_2719.png" alt="Logo" class="logo-img mb-4" />
      
      <h2 class="text-h5 font-weight-bold mb-4">Create a New Post</h2>

      <div class="w-100 text-left">
        <v-textarea
          v-model="postContent"
          label="Post Content"
          variant="outlined"
          rows="4"
          placeholder="What's on your mind?"
          color="orangered"
        ></v-textarea>

        <v-file-input
          v-model="imageFile"
          label="Image File"
          accept="image/*"
          variant="outlined"
          prepend-icon="mdi-camera"
          color="orangered"
          @update:model-value="handleFileChange"
        ></v-file-input>

        <div v-if="previewUrl" class="mb-4 text-center">
            <img :src="previewUrl" alt="Preview" style="max-width: 200px; border-radius: 8px; border: 1px solid #ccc;" />
        </div>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-4 w-100">
        {{ error }}
      </v-alert>

      <v-btn 
        color="orangered" 
        block 
        class="mb-3 text-white" 
        size="large"
        :loading="loading"
        @click="createPost"
      >
        Create Post
      </v-btn>

    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const postContent = ref('')
const imageFile = ref<File[] | undefined>() 
const previewUrl = ref<string | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

function handleFileChange(files: File | File[]) {
    const file = Array.isArray(files) ? files[0] : files;
    if (file) {
      imageFile.value = [file]
        previewUrl.value = URL.createObjectURL(file)
    } else {
        previewUrl.value = null
    }
}

async function createPost() {
    loading.value = true
    error.value = null

    try {
        const authorId = userStore.user?.username || sessionStorage.getItem("loggedInUser")
        
        if (!authorId) {
            throw new Error("You must be logged in to post.")
        }

        const formData = new FormData()
        formData.append("authorId", authorId)
        formData.append("content", postContent.value)
        
        if (imageFile.value && imageFile.value[0]) {
            formData.append("imageFile", imageFile.value[0])
        }

        const response = await fetch('/api/posts', {
            method: 'POST',
            body: formData
        })

        if (response.status === 401) {
            router.push('/login')
            return
        }

        if (!response.ok) {
            const data = await response.json().catch(() => ({}))
            throw new Error(data.message || "Error creating post")
        }

        alert('Post created successfully!')
        router.push('/view-posts')

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
    width: 120px;
    height: auto;
}

.action-link {
    text-decoration: none;
    font-weight: bold;
}
</style>