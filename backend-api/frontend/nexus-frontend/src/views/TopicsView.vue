<template>
  <div class="center-container">
    <div class="header-section">
      <h3>Trending Topics</h3>
      <button @click="fetchTopics" class="refresh-btn">
        <span v-if="isLoading">...</span>
        <span v-else>Refresh</span>
      </button>
    </div>
    
    <div class="cloud-wrapper">
      <div v-if="isLoading && words.length === 0" class="loading">
        <p>Loading topics...</p>
      </div>

      <vue-word-cloud
        v-else-if="words.length > 0"
        :words="words"
        :color="getColor"
        font-family="Roboto, sans-serif"
        :spacing="0.8" 
        :font-size-ratio="6"
      >
        <template v-slot="{text: topic, weight: count}">
          <div 
            class="cloud-word" 
            :title="`${topic}: ${count} posts`"
            @click="goToTopic(topic)"
          >
            {{ topic }}
          </div>
        </template>
      </vue-word-cloud>

      <div v-else class="loading">
        <p>No trending topics found.</p>
        <p style="font-size: 12px; margin-top: 5px; color: #aaa;">
            Create a post to generate data!
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import VueWordCloud from 'vuewordcloud';

export default {
  name: 'TopicWordCloud',
  components: {
    VueWordCloud,
  },
  data() {
    return {
      words: [],
      isLoading: false,
    };
  },
  mounted() {
    this.fetchTopics();
  },
  methods: {
    async fetchTopics() {
      this.isLoading = true;
      try {
        const response = await fetch('http://localhost:8080/topics');
        const data = await response.json();
        this.words = data.map(item => [item.topic, item.count]);
      } catch (error) {
        console.error("Failed to load word cloud", error);
      } finally {
        this.isLoading = false;
      }
    },
    goToTopic(topic) {
      this.$router.push(`/topics/${topic}`);
    },
    getColor([, count]) {
      if (count >= 20) return '#BF360C'; 
      if (count >= 10) return '#E65100'; 
      if (count >= 5)  return '#F57C00'; 
      return '#FFB74D';                  
    }
  }
};
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
  padding: 40px 20px;
}

.header-section {
  display: flex; 
  align-items: center; 
  gap: 15px; 
  margin-bottom: 20px;
}

h3 {
  margin: 0;
  color: #333;
  font-family: 'Roboto', sans-serif;
  font-size: 24px;
}

.cloud-wrapper {
  height: 500px;
  width: 800px; 
  position: relative;
  overflow: visible;
  margin: 0 auto;   
}

.cloud-word {
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: bold;
  background: transparent; 
  text-shadow: 1px 1px 0px rgba(0,0,0,0.05);
}

.cloud-word:hover {
  opacity: 0.8;
  transform: scale(1.1);
}

.refresh-btn {
  background: transparent;
  border: 1px solid #ddd;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 11px;
  color: #888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.refresh-btn:hover {
  border-color: #F57C00;
  color: #F57C00;
}

.loading {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-style: italic;
}
</style>