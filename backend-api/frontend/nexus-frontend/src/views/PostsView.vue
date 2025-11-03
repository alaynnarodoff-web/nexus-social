<template>
  <div class="posts-container">
    <h1>All Posts</h1>
    <p>Welcome, {{ username }}!</p>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const username = ref(sessionStorage.getItem('loggedInUser') || 'User')
</script>

<style scoped>
.posts-container {
  padding: 2rem;
  text-align: center;
}
</style>