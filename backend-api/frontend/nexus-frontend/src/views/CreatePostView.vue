<template>
  <div class="center-container">
    <v-container class="d-flex flex-column align-center text-center" style="max-width: 500px;">
      
      <img src="@/assets/thumbnail_IMG_2719.png" alt="Logo" class="logo-img mb-4" />
      
      <h2 class="text-h5 font-weight-bold mb-4">Create a New Post</h2>

      <div class="w-100">
        <v-textarea
          v-model="postContent"
          label="What do you want to say?"
          variant="outlined"
          rows="4"
          color="orangered"
          class="mb-6 post-text-area" 
          placeholder="Type your message here..."
          auto-grow
        ></v-textarea>

        <div class="d-flex flex-column align-center mb-6">
            
            <div v-if="previewBase64" class="mb-4 d-flex flex-column align-center">
                <p class="text-caption text-grey mb-2">Image Preview</p>
                <v-img 
                  :src="previewBase64" 
                  width="250" 
                  class="rounded-lg border shadow-sm mb-2"
                  cover
                />
                <v-btn
                    variant="text"
                    color="grey-darken-1"
                    size="small"
                    prepend-icon="mdi-close"
                    @click="clearPhoto"
                >
                    Remove Photo
                </v-btn>
            </div>

            <div 
                class="d-flex align-center justify-center upload-trigger-btn pa-2" 
                @click="triggerFileInput"
                v-ripple
            >
                 <v-icon color="orangered" icon="mdi-camera" class="mr-2"></v-icon>
                 <span class="text-body-2 font-weight-bold" style="color: orangered;">
                    {{ previewBase64 ? 'Change Photo' : 'Add Photo' }}
                 </span>
            </div>

            <v-file-input
                ref="fileInputRef"
                v-model="imageFile"
                accept="image/*"
                hide-details
                class="d-none"
                @update:model-value="handleFileChange"
            ></v-file-input>
        </div>
      </div>

      <v-alert v-if="error" type="error" variant="tonal" class="mb-4 w-100">
        {{ error }}
      </v-alert>

      <v-btn 
        block 
        elevation="4"
        height="54"
        :loading="loading"
        @click="createPost"
        style="background-color: orangered !important; color: white !important;"
      >
        <span style="color: white !important; font-weight: bold; font-size: 1.1rem;">
          CREATE POST
        </span>
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
const loading = ref(false)
const error = ref<string | null>(null)

const fileInputRef = ref<any>(null)
const imageFile = ref<any>(undefined) 
const previewBase64 = ref<string | null>(null)

function triggerFileInput() {
    fileInputRef.value?.click()
}

function handleFileChange(files: File | File[]) {
    const file = Array.isArray(files) ? files[0] : files;
    if (file) {
        imageFile.value = file; 
        const reader = new FileReader();
        reader.onload = (e) => {
            if (typeof e.target?.result === 'string') {
                previewBase64.value = e.target.result;
            }
        }
        reader.readAsDataURL(file);
    }
}

function clearPhoto() {
    imageFile.value = undefined;
    previewBase64.value = null;
}

async function createPost() {
    if (!postContent.value.trim() && !imageFile.value) {
        error.value = "Please add some text or a photo before posting.";
        return;
    }

    loading.value = true
    error.value = null

    try {
        const authorId = userStore.user?.username || sessionStorage.getItem("loggedInUser")
        if (!authorId) throw new Error("You must be logged in to post.")

        const formData = new FormData()
        formData.append("authorId", authorId)
        formData.append("content", postContent.value || "")
        
        const fileToUpload = Array.isArray(imageFile.value) ? imageFile.value[0] : imageFile.value;
        if (fileToUpload) {
            formData.append("imageFile", fileToUpload)
        }

        const response = await fetch('/api/posts', {
            method: 'POST',
            body: formData
        })

        if (!response.ok) {
            const data = await response.json().catch(() => ({}))
            throw new Error(data.message || "Error creating post")
        }

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

.upload-trigger-btn {
    cursor: pointer;
    border-radius: 8px;
    transition: background-color 0.2s ease;
    display: inline-flex;
}

.upload-trigger-btn:hover {
    background-color: #fff3e6;
}

.post-text-area :deep(.v-field__outline) {
  color: orangered !important;
}

.post-text-area :deep(.v-field__input) {
  min-height: 100px;
}
</style>