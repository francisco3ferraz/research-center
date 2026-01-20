<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Administration - Hidden Content</h1>

    <!-- Tabs -->
    <div class="border-b border-gray-200 mb-6">
      <nav class="-mb-px flex space-x-8">
        <button 
          v-for="tab in tabs" 
          :key="tab.id"
          @click="activeTab = tab.id"
          class="py-2 px-1 border-b-2 font-medium text-sm transition-colors"
          :class="activeTab === tab.id 
            ? 'border-blue-500 text-blue-600' 
            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'"
        >
          {{ tab.label }} ({{ getCount(tab.id) }})
        </button>
      </nav>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-2 text-gray-500">Loading...</p>
    </div>

    <!-- Publications Tab -->
    <div v-else-if="activeTab === 'publications'">
      <div v-if="hiddenPublications.length === 0" class="text-center py-12 bg-gray-50 rounded-lg">
        <p class="text-gray-500">No hidden publications.</p>
      </div>
      <div v-else class="space-y-4">
        <div v-for="pub in hiddenPublications" :key="pub.id" class="bg-white rounded-lg shadow p-4 border-l-4 border-yellow-500">
          <div class="flex justify-between items-start">
            <div>
              <h3 class="font-medium text-gray-900">{{ pub.title }}</h3>
              <p class="text-sm text-gray-500 mt-1">
                <span class="font-medium">Authors:</span> {{ pub.authors?.join(', ') || 'N/A' }}
              </p>
              <p class="text-sm text-gray-500">
                <span class="font-medium">Hidden by:</span> {{ pub.hiddenBy?.name || 'N/A' }}
              </p>
            </div>
            <NuxtLink :to="`/publications/${pub.id}`" class="text-blue-600 hover:underline text-sm">
              View details
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>

    <!-- Comments Tab -->
    <div v-else-if="activeTab === 'comments'">
      <div v-if="hiddenComments.length === 0" class="text-center py-12 bg-gray-50 rounded-lg">
        <p class="text-gray-500">No hidden comments.</p>
      </div>
      <div v-else class="space-y-4">
        <div v-for="comment in hiddenComments" :key="comment.id" class="bg-white rounded-lg shadow p-4 border-l-4 border-orange-500">
          <div class="flex justify-between items-start">
            <div>
              <p class="text-gray-700">{{ comment.content }}</p>
              <p class="text-sm text-gray-500 mt-2">
                <span class="font-medium">Author:</span> {{ comment.author?.name || 'N/A' }}
              </p>
              <p class="text-sm text-gray-500">
                <span class="font-medium">Publication:</span> {{ comment.publicationTitle || 'N/A' }}
              </p>
              <p class="text-sm text-gray-500">
                <span class="font-medium">Hidden by:</span> {{ comment.hiddenBy?.name || 'N/A' }}
              </p>
            </div>
            <NuxtLink v-if="comment.publicationId" :to="`/publications/${comment.publicationId}`" class="text-blue-600 hover:underline text-sm">
              View publication
            </NuxtLink>
          </div>
        </div>
      </div>
    </div>

    <!-- Tags Tab -->
    <div v-else-if="activeTab === 'tags'">
      <div v-if="hiddenTags.length === 0" class="text-center py-12 bg-gray-50 rounded-lg">
        <p class="text-gray-500">No hidden tags.</p>
      </div>
      <div v-else class="space-y-4">
        <div v-for="tag in hiddenTags" :key="tag.id" class="bg-white rounded-lg shadow p-4 border-l-4 border-red-500">
          <div class="flex justify-between items-start">
            <div>
              <h3 class="font-medium text-gray-900">{{ tag.name }}</h3>
              <p v-if="tag.description" class="text-sm text-gray-500 mt-1">{{ tag.description }}</p>
            </div>
            <NuxtLink to="/tags" class="text-blue-600 hover:underline text-sm">
              Manage tags
            </NuxtLink>
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
const auth = useAuth()

// Check if user is admin or manager
const canAccess = computed(() => {
  const role = auth.user.value?.role
  return role === 'ADMINISTRADOR' || role === 'RESPONSAVEL'
})

const tabs = [
  { id: 'publications', label: 'Publications' },
  { id: 'comments', label: 'Comments' },
  { id: 'tags', label: 'Tags' }
]

const activeTab = ref('publications')
const loading = ref(true)
const error = ref(null)
const hiddenPublications = ref([])
const hiddenComments = ref([])
const hiddenTags = ref([])

const getCount = (tabId) => {
  switch (tabId) {
    case 'publications': return hiddenPublications.value.length
    case 'comments': return hiddenComments.value.length
    case 'tags': return hiddenTags.value.length
    default: return 0
  }
}

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const [pubsResponse, commentsResponse, tagsResponse] = await Promise.all([
      api.get('/admin/hidden-publications'),
      api.get('/admin/hidden-comments'),
      api.get('/admin/hidden-tags')
    ])
    hiddenPublications.value = pubsResponse.data || []
    hiddenComments.value = commentsResponse.data || []
    hiddenTags.value = tagsResponse.data || []
  } catch (e) {
    console.error('Failed to fetch hidden content', e)
    if (e.response?.status === 403) {
      error.value = 'You do not have permission to access this page.'
    } else {
      error.value = 'Error loading hidden content.'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>
