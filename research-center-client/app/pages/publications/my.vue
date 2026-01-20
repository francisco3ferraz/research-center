<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-2xl font-bold text-slate-800">My Publications</h2>
          <p class="text-sm text-slate-500">All publications submitted by me (including non-visible ones)</p>
        </div>
        <NuxtLink to="/publications/create" class="bg-green-600 text-white px-4 py-2 rounded shadow">
          + New Publication
        </NuxtLink>
      </div>

      <div v-if="loading" class="text-slate-600">Loading...</div>
      <div v-else>
        <div v-if="items.length === 0" class="text-center py-12 text-slate-500">
          <div class="text-4xl mb-3">📄</div>
          <div>You haven't submitted any publications yet</div>
          <NuxtLink to="/publications/create" class="text-blue-600 hover:underline mt-2 inline-block">
            Submit first publication
          </NuxtLink>
        </div>

        <div v-else class="space-y-4">
          <div v-for="p in items" :key="p.id" class="border rounded-lg p-4 hover:shadow-lg transition">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <NuxtLink :to="`/publications/${p.id}`" class="text-lg text-slate-800 font-semibold hover:text-blue-600">
                    {{ p.title }}
                  </NuxtLink>
                  <span v-if="!p.visible" class="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs rounded">
                    Hidden
                  </span>
                  <span v-if="p.confidential" class="px-2 py-1 bg-red-100 text-red-800 text-xs rounded">
                    Confidential
                  </span>
                </div>
                <div class="text-sm text-slate-500 mt-1">
                  {{ (p.authors || []).join(', ') }} — {{ p.year || '—' }}
                </div>
                <div class="flex gap-2 mt-2">
                  <span v-for="tag in p.tags || []" :key="tag.id" class="px-2 py-1 bg-gray-100 text-slate-700 text-xs rounded-full">
                    {{ tag.name }}
                  </span>
                </div>
              </div>
              <div class="flex flex-col items-end gap-2">
                <NuxtLink :to="`/publications/${p.id}/edit`" class="text-blue-600 hover:underline text-sm">
                  Edit
                </NuxtLink>
                <div class="text-xs text-slate-400">
                  {{ p.uploadedAt ? new Date(p.uploadedAt).toLocaleDateString() : '' }}
                </div>
              </div>
            </div>
            <div class="flex items-center gap-4 mt-3 text-sm text-slate-600">
              <div>⭐ {{ p.averageRating?.toFixed?.(2) || '—' }}</div>
              <div>💬 {{ p.commentsCount || 0 }}</div>
              <div>👁 {{ p.viewsCount || 0 }} views</div>
            </div>
          </div>
        </div>

        <div v-if="items.length > 0" class="flex justify-between items-center mt-6">
          <div class="text-sm text-slate-600">{{ items.length }} of {{ totalElements }} publications</div>
          <div class="flex gap-2">
            <button :disabled="page<=0" @click="prevPage" class="px-3 py-1 border rounded bg-white disabled:opacity-50">
              Previous
            </button>
            <button :disabled="page+1>=totalPages" @click="nextPage" class="px-3 py-1 border rounded bg-white disabled:opacity-50">
              Next
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
const size = ref(10)
const totalPages = ref(1)
const totalElements = ref(0)

const fetchMyPubs = async () => {
  // Rely on API 401
  loading.value = true
  try {
    const resp = await api.get('/publications/my-publications', { params: { page: page.value, size: size.value } })
    const data = resp.data
    items.value = data.content || []
    totalPages.value = data.totalPages || 1
    totalElements.value = data.totalElements || 0
  } catch (e) {
    console.error(e)
    if (e?.response?.status === 401) navigateTo('/auth/login')
  } finally {
    loading.value = false
  }
}

const prevPage = () => { if (page.value>0) { page.value--; fetchMyPubs() } }
const nextPage = () => { if (page.value+1<totalPages.value) { page.value++; fetchMyPubs() } }

onMounted(fetchMyPubs)
</script>
