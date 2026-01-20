<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">My Subscriptions</h1>
        <p class="text-gray-500 mt-1">Receive notifications when there are new publications in subscribed tags.</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-2 text-gray-500">Loading subscriptions...</p>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 gap-8">
      <!-- Subscribed Tags -->
      <div>
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Subscribed Tags ({{ subscriptions.length }})</h2>
        
        <div v-if="subscriptions.length === 0" class="bg-gray-50 rounded-lg p-6 text-center">
          <div class="text-gray-400 text-4xl mb-2">📭</div>
          <p class="text-gray-500">You haven't subscribed to any tags yet.</p>
        </div>

        <div v-else class="space-y-2">
          <div 
            v-for="tag in subscriptions" 
            :key="tag.id"
            class="flex items-center justify-between bg-white rounded-lg shadow p-4"
          >
            <div class="flex items-center gap-3">
              <span class="text-2xl">🏷️</span>
              <div>
                <h3 class="font-medium text-gray-900">{{ tag.name }}</h3>
                <p v-if="tag.description" class="text-sm text-gray-500">{{ tag.description }}</p>
              </div>
            </div>
            <button 
              @click="unsubscribe(tag.id)"
              :disabled="actionLoading === tag.id"
              class="px-3 py-1.5 text-sm text-red-600 border border-red-300 rounded-md hover:bg-red-50 disabled:opacity-50"
            >
              {{ actionLoading === tag.id ? '...' : 'Unsubscribe' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Available Tags to Subscribe -->
      <div>
        <h2 class="text-lg font-semibold text-gray-900 mb-4">Available Tags</h2>
        
        <!-- Search -->
        <div class="mb-4">
          <input 
            v-model="searchQuery"
            type="text"
            placeholder="Search tags..."
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          >
        </div>

        <div v-if="availableTags.length === 0" class="bg-gray-50 rounded-lg p-6 text-center">
          <p class="text-gray-500">All tags are already subscribed or no tags exist.</p>
        </div>

        <div v-else class="space-y-2 max-h-96 overflow-y-auto">
          <div 
            v-for="tag in filteredAvailableTags" 
            :key="tag.id"
            class="flex items-center justify-between bg-white rounded-lg shadow p-4"
          >
            <div class="flex items-center gap-3">
              <span class="text-2xl">🏷️</span>
              <div>
                <h3 class="font-medium text-gray-900">{{ tag.name }}</h3>
                <p v-if="tag.description" class="text-sm text-gray-500 line-clamp-1">{{ tag.description }}</p>
              </div>
            </div>
            <button 
              @click="subscribe(tag.id)"
              :disabled="actionLoading === tag.id"
              class="px-3 py-1.5 text-sm text-white bg-blue-600 rounded-md hover:bg-blue-500 disabled:opacity-50"
            >
              {{ actionLoading === tag.id ? '...' : 'Subscribe' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="mt-4 p-4 bg-red-50 text-red-600 rounded-lg">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: ['auth']
})

const api = useApi()
const subscriptions = ref([])
const allTags = ref([])
const loading = ref(true)
const actionLoading = ref(null)
const error = ref(null)
const searchQuery = ref('')

const availableTags = computed(() => {
  const subscribedIds = new Set(subscriptions.value.map(s => s.id))
  return allTags.value.filter(tag => !subscribedIds.has(tag.id))
})

const filteredAvailableTags = computed(() => {
  if (!searchQuery.value) return availableTags.value
  const query = searchQuery.value.toLowerCase()
  return availableTags.value.filter(tag => 
    tag.name.toLowerCase().includes(query) || 
    (tag.description && tag.description.toLowerCase().includes(query))
  )
})

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const [subsResponse, tagsResponse] = await Promise.all([
      api.get('/subscriptions/tags'),
      api.get('/tags')
    ])
    subscriptions.value = subsResponse.data || []
    allTags.value = tagsResponse.data || []
  } catch (e) {
    console.error('Failed to fetch data', e)
    error.value = 'Error loading data.'
  } finally {
    loading.value = false
  }
}

const subscribe = async (tagId) => {
  actionLoading.value = tagId
  try {
    await api.post('/subscriptions/tags', { tagId })
    // Move tag from available to subscribed
    const tag = allTags.value.find(t => t.id === tagId)
    if (tag) {
      subscriptions.value.push(tag)
    }
  } catch (e) {
    console.error('Failed to subscribe', e)
    error.value = 'Error subscribing to tag.'
  } finally {
    actionLoading.value = null
  }
}

const unsubscribe = async (tagId) => {
  actionLoading.value = tagId
  try {
    await api.delete(`/subscriptions/tags/${tagId}`)
    subscriptions.value = subscriptions.value.filter(s => s.id !== tagId)
  } catch (e) {
    console.error('Failed to unsubscribe', e)
    error.value = 'Error unsubscribing from tag.'
  } finally {
    actionLoading.value = null
  }
}

onMounted(() => {
  fetchData()
})
</script>
