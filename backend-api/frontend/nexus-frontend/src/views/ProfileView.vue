<template>
  <div class="center-container">
    <v-container v-if="loadingProfile" class="d-flex justify-center align-center" style="height: 50vh;">
      <v-progress-circular indeterminate color="orangered"></v-progress-circular>
    </v-container>

    <v-container v-else-if="error" class="text-center">
      <h3 class="text-red">{{ error }}</h3>
      <v-btn to="/view-posts" variant="text" color="orangered">Go Home</v-btn>
    </v-container>

    <v-container v-else-if="profileUser" class="d-flex flex-column align-center text-center">
      <div class="content-wrapper">
        
        <div class="d-flex flex-column align-center mb-6">
          <v-avatar
            size="120"
            color="grey-lighten-3"
            class="mb-3 elevation-3"
            style="border: 3px solid orangered"
          >
            <v-img
              :src="profileUser.avatar || 'defaultAvatar.jpg'"
              alt="User avatar"
              cover
              @error="profileUser!.avatar = 'defaultAvatar.jpg'"
            />
          </v-avatar>
          
          <h2 class="text-h4 font-weight-bold text-grey-darken-3">{{ profileUser.username }}</h2>
          <p class="text-subtitle-1 text-grey">{{ profileUser.firstName }} {{ profileUser.lastName }}</p>
          
          <v-chip class="mt-2" color="orangered" variant="outlined" size="small">
            {{ profileUser.friendCount || 0 }} Friends
          </v-chip>
        </div>

        <v-card variant="tonal" class="pa-4 mb-4 text-left bg-grey-lighten-4 border-none">
          <div class="text-caption font-weight-bold text-grey-darken-1 mb-1">About</div>
          <div class="text-body-1 text-grey-darken-3">
            {{ profileUser.bio || 'This user has not written a bio yet.' }}
          </div>
        </v-card>

        <v-card variant="tonal" class="pa-4 mb-6 text-left bg-grey-lighten-4 border-none">
          <div class="text-caption font-weight-bold text-grey-darken-1 mb-1">Topics of Interest</div>
          <UserInterestCloud :username="profileUser.username" />
        </v-card>

        <div class="d-flex justify-center gap-2 mb-8">
            <router-link 
              v-if="isCurrentUser" 
              to="/account" 
              class="v-btn v-btn--elevated bg-orangered text-white px-6"
              style="text-decoration: none; height: 36px; line-height: 36px; border-radius: 4px;"
            >
              Edit Profile
            </router-link>

            <div v-else>
              <v-chip v-if="friendshipStatus === 'friends'" color="success" prepend-icon="mdi-account-check" variant="flat">
                Already Friends
              </v-chip>

              <v-chip v-else-if="friendshipStatus === 'pending'" color="orange" variant="outlined" prepend-icon="mdi-clock-outline">
                Request Pending
              </v-chip>

              <v-btn 
                v-else 
                variant="flat" 
                color="orangered" 
                prepend-icon="mdi-account-plus"
                :loading="requesting"
                @click="sendFriendRequest"
              >
                Add Friend
              </v-btn>
            </div>
        </div>

        <v-divider class="mb-6"></v-divider>

        <h3 class="text-h5 font-weight-bold mb-4 text-left align-self-start w-100">
          Posts by {{ profileUser.username }}
        </h3>

        <div v-if="loadingPosts" class="my-4">
           <v-progress-circular indeterminate color="orangered" size="30"></v-progress-circular>
        </div>

        <div v-else-if="posts.length === 0" class="text-grey font-italic my-4">
           No posts to show.
        </div>

        <div v-else class="w-100">
          <v-card
            v-for="post in posts"
            :key="post.id"
            class="mb-5 mx-auto post-card text-left"
            variant="outlined"
          >
            <v-list-item class="py-3">
              <template v-slot:prepend>
                <v-avatar size="52" class="mr-3">
                  <v-img :src="post.authorAvatar || 'defaultAvatar.jpg'" cover />
                </v-avatar>
              </template>
              <v-list-item-title class="font-weight-bold">{{ post.authorId }}</v-list-item-title>
              <v-list-item-subtitle>{{ new Date(post.timestamp).toLocaleString() }}</v-list-item-subtitle>
            </v-list-item>
            <v-divider />
            <v-card-text class="text-body-1 py-4" style="color: black">{{ post.content }}</v-card-text>
          </v-card>
        </div>
      </div>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useUserStore, type UserProfile } from '@/stores/user'
import { usePostsStore } from '@/stores/posts'
import { storeToRefs } from 'pinia'
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import UserInterestCloud from '@/components/UserInterestCloud.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const postStore = usePostsStore()

const { user: loggedInUser } = storeToRefs(userStore)
const { posts } = storeToRefs(postStore)

const profileUser = ref<(UserProfile & { friendCount?: number }) | null>(null)
const friendshipStatus = ref<'none' | 'friends' | 'pending'>('none')
const loadingProfile = ref<boolean>(true)
const loadingPosts = ref<boolean>(false)
const requesting = ref<boolean>(false)
const error = ref<string | null>(null)

const props = defineProps<{
  username: string
}>()

const isCurrentUser = computed(() => {
  return loggedInUser.value?.username === props.username
})


async function checkFriendshipStatus() {
  if (isCurrentUser.value || !loggedInUser.value) return;
  try {
    const res = await fetch(`/api/friends/areFriends?user1=${loggedInUser.value.username}&user2=${props.username}`, {
      credentials: 'include'
    });
    const areFriends = await res.json();
    friendshipStatus.value = areFriends ? 'friends' : 'none';
    
  } catch (err) {
    console.error("Failed to check friendship status", err);
  }
}

async function sendFriendRequest() {
    requesting.value = true;
    try {
        const res = await fetch(`/api/friends/request?toUser=${props.username}`, {
            method: 'POST',
            credentials: 'include'
        });

        if (res.ok) {
            alert(`Friend request sent to ${props.username}!`);
            friendshipStatus.value = 'pending';
        } else {
            const msg = await res.text();
            alert(msg || "Failed to send request.");
        }
    } catch (err) {
        alert("An error occurred while sending the request.");
    } finally {
        requesting.value = false;
    }
}

const loadData = async (usernameToFetch: string) => {
  loadingProfile.value = true
  loadingPosts.value = true
  error.value = null
  profileUser.value = null
  
  try {
    if (!loggedInUser.value) {
        await userStore.fetchUser()
    }

    let userData: any;
    let count: number = 0;

    if (isCurrentUser.value) {
        userData = loggedInUser.value;
        await userStore.fetchFriendCount(); 
        count = userStore.friendCount;
    } else {
        userData = await userStore.fetchUserByUsername(usernameToFetch);
        if (!userData) throw new Error('User not found');
        
        count = await userStore.fetchFriendCount(usernameToFetch);
        
        await checkFriendshipStatus();
    }

    profileUser.value = { ...userData, friendCount: count };
    await postStore.fetchUserPosts(usernameToFetch);

  } catch (err: any) {
    error.value = err.message || 'Could not load profile'
  } finally {
    loadingProfile.value = false
    loadingPosts.value = false
  }
}

watch(() => props.username, (newUsername) => {
  if (newUsername) loadData(newUsername)
})

onMounted(() => {
  loadData(props.username)
})

function goToProfile(targetUsername: string) {
  if (targetUsername && targetUsername !== props.username) {
    router.push(`/profile/${targetUsername}`)
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
  margin: 0;
  padding: 0;
}
.content-wrapper {
    width: 100%;
    max-width: 700px;
}
.post-card {
  width: 100%;
  max-width: 700px;
}
</style>