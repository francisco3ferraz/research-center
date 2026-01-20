<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-2xl font-bold text-slate-800">Activity History</h2>
          <p class="text-sm text-slate-500">Record of your actions on the platform</p>
        </div>
        <NuxtLink to="/profile" class="text-blue-600 hover:underline text-sm">Back to Profile</NuxtLink>
      </div>

      <div v-if="loading" class="text-slate-600">Loading history...</div>
      <div v-else>
        <div v-if="items.length === 0" class="text-center py-12 text-slate-500">
          <div class="text-4xl mb-3">📋</div>
          <div>No activity recorded</div>
        </div>

        <div v-else class="space-y-3">
          <div v-for="log in items" :key="log.id" class="border-l-4 pl-4 py-3" :class="getActivityColor(log.actionType)">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-slate-800">{{ getActionLabel(log.actionType) }}</span>
                  <span class="text-sm text-slate-500">{{ log.entityType }}</span>
                </div>
                <div class="text-sm text-slate-600 mt-1">{{ log.description }}</div>
                <div v-if="log.changedFields" class="text-xs text-slate-400 mt-1">
                  Changed fields: {{ log.changedFields }}
                </div>
              </div>
              <div class="text-xs text-slate-400 whitespace-nowrap ml-4">
                {{ log.timestamp ? new Date(log.timestamp).toLocaleString() : '' }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="items.length > 0" class="flex justify-between items-center mt-6 pt-4 border-t">
          <div class="text-sm text-slate-600">Showing {{ items.length }} activities</div>
          <div class="flex gap-2">
            <button @click="loadMore" v-if="hasMore" class="px-4 py-2 border rounded bg-white hover:bg-gray-50">
              Load More
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

definePageMeta({
  middleware: 'auth'
})

const api = useApi()
const auth = useAuth()

const items = ref([])
const loading = ref(false)
const page = ref(0)
const size = ref(20)
const hasMore = ref(true)

const fetchActivity = async () => {
  // Rely on API 401 for redirect
  loading.value = true
  try{
    const resp = await api.get('/activity-logs/my', { params: { page: page.value, size: size.value } })
    const data = resp.data
    if (page.value === 0) {
      items.value = data.content || []
    } else {
      items.value = [...items.value, ...(data.content || [])]
    }
    hasMore.value = (page.value + 1) < (data.totalPages || 0)
  } catch (e) {
    console.error(e)
    if (e?.response?.status === 401) navigateTo('/auth/login')
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchActivity()
}

const getActionLabel = (actionType) => {
  const labels = {
    'CREATE': '➕ Created',
    'UPDATE': '✏️ Updated',
    'DELETE': '🗑️ Removed',
    'UPLOAD': '📤 Upload',
    'COMMENT': '💬 Comment',
    'RATE': '⭐ Rating'
  }
  return labels[actionType] || actionType
}

const getActivityColor = (actionType) => {
  const colors = {
    'CREATE': 'border-green-400',
    'UPDATE': 'border-blue-400',
    'DELETE': 'border-red-400',
    'UPLOAD': 'border-purple-400',
    'COMMENT': 'border-yellow-400',
    'RATE': 'border-orange-400'
  }
  return colors[actionType] || 'border-gray-400'
}

onMounted(fetchActivity)
</script>
