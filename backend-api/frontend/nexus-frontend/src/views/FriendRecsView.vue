<template>
  <div v-if="fof.length > 0" class="py-2 w-100 d-flex justify-center">
    
    <v-sheet
      class="mx-auto"
      max-width="670"
      color="transparent"
    >
      <v-slide-group
        show-arrows="always"
        center-active
      >
        <template v-slot:prev>
            <v-btn 
                icon="mdi-chevron-left" 
                color="orangered" 
                variant="text" 
                size="x-large"
            ></v-btn>
        </template>

        <template v-slot:next>
            <v-btn 
                icon="mdi-chevron-right" 
                color="orangered" 
                variant="text" 
                size="x-large"
            ></v-btn>
        </template>

        <v-slide-group-item
          v-for="friend in fof"
          :key="friend.username"
        >
          <v-card
            class="ma-2 d-flex flex-column align-center pt-4"
            width="180"
            height="260"
            elevation="2"
            rounded="lg"
            @click="router.push(`/profile/${friend.username}`)"
            style="cursor: pointer; transition: transform 0.2s; border: 1px solid #eee;"
          >
              <v-avatar size="80" style="border: 2px solid orangered">
                  <v-img 
                      :src="friend.avatar || 'defaultAvatar.jpg'" 
                      cover
                      alt="Avatar"
                      @error="friend.avatar = 'defaultAvatar.jpg'"
                  ></v-img>
              </v-avatar>

              <v-card-title class="text-subtitle-1 font-weight-bold mt-2 pt-0 text-center">
                  {{ friend.username }}
              </v-card-title>
              
              <v-card-subtitle class="text-caption pb-1 text-center text-grey-darken-1">
                  {{ friend.firstName }} {{ friend.lastName }}
              </v-card-subtitle>

              <v-spacer></v-spacer>

              <v-card-actions class="w-100 pb-3 px-3">
                  <v-btn
                      block
                      variant="flat"
                      color="orangered"
                      size="small"
                      class="text-white text-capitalize"
                      @click.stop="addFriend(friend.username)"
                  >
                      Add Friend
                  </v-btn>
              </v-card-actions>
          </v-card>
        </v-slide-group-item>
      </v-slide-group>
    </v-sheet>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const userStore = useUserStore()
const { fof, user } = storeToRefs(userStore)

const loading = ref(true)
const error = ref<string | null>(null)

watch(user, async (newUser) => {
  try {
    if (newUser?.username) {
      await userStore.fetchFof(newUser.username)
    }
  } catch (err: any) {
    error.value = (err as Error)?.message ?? String(err)
  } finally {
    loading.value = false
  }
}, { immediate: true })

function addFriend(username: string) {
    alert(`Friend request sent to ${username}!`)
}
</script>

<style scoped>
.v-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 15px rgba(0,0,0,0.1) !important;
}
</style>