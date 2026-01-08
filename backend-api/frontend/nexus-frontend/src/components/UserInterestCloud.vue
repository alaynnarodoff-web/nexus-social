<template>
  <div class="cloud-widget">
    <div v-if="loading" class="text-center text-grey text-caption">
      Analyzing interests...
    </div>

    <div v-else-if="words.length === 0" class="text-center text-grey text-caption">
      No interactions yet.
    </div>

    <vue-word-cloud
      v-else
      :words="words"
      :color="getColor"
      font-family="Roboto, sans-serif"
      :spacing="0.6"
      :font-size-ratio="5"
    >
      <template v-slot="{text: topic, weight: count}">
        <div 
          class="cloud-word" 
          :title="`${topic}: Interacted ${count} times`"
          @click="goToTopic(topic)"
        >
          {{ topic }}
        </div>
      </template>
    </vue-word-cloud>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import VueWordCloud from 'vuewordcloud';

// Receive username as a prop
const props = defineProps<{ username: string }>();
const router = useRouter();

const words = ref([]);
const loading = ref(false);

const fetchInterests = async () => {
  if (!props.username) return;
  
  loading.value = true;
  try {
    const res = await fetch(`/api/users/${props.username}/interests`);
    if (res.ok) {
      const data = await res.json();
      words.value = data.map((item: any) => [item.topic, item.count]);
    }
  } catch (e) {
    console.error("Failed to load user interests", e);
  } finally {
    loading.value = false;
  }
};

const getColor = ([, count]: any) => {
  if (count >= 20) return '#BF360C'; 
  if (count >= 10) return '#E65100'; 
  if (count >= 5)  return '#F57C00'; 
  return '#FFB74D';                
};

const goToTopic = (topic: string) => {
  router.push(`/topics/${topic}`);
};
onMounted(fetchInterests);

watch(() => props.username, fetchInterests);
</script>

<style scoped>
.cloud-widget {
  height: 250px; 
  width: 100%;
  position: relative;
  overflow: visible; 
}

.cloud-word {
  cursor: pointer;
  font-weight: bold;
  background: transparent;
  transition: transform 0.2s;
}

.cloud-word:hover {
  transform: scale(1.1);
  opacity: 0.8;
}
</style>