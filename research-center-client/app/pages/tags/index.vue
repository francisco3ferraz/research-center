<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-2xl font-semibold">Tag Management</h2>
          <p class="text-sm text-slate-500">Create, edit and remove tags (MANAGER / ADMIN)</p>
        </div>
        <div>
          <button @click="openCreate" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">+ New Tag</button>
        </div>
      </div>

      <div v-if="loading" class="text-slate-600">Loading tags...</div>

      <div v-else>
        <ul class="space-y-2">
          <li v-for="t in tags" :key="t.id" class="flex items-center justify-between p-3 border rounded-md hover:bg-gray-50">
            <div>
              <div class="font-medium text-gray-900">{{ t.name }}</div>
              <div class="text-sm text-slate-500">{{ t.description }}</div>
            </div>
            <div class="flex items-center gap-3">
              <button @click="openEdit(t)" class="text-blue-600 hover:text-blue-900">Edit</button>
              <button @click="removeTag(t)" class="text-red-600 hover:text-red-800">Remove</button>
            </div>
          </li>
        </ul>
      </div>

      <div v-if="error" class="text-red-600 mt-3">{{ error }}</div>
    </div>

    <!-- Modal -->
    <transition name="fade">
      <div v-if="showForm" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-40">
        <div class="bg-white rounded-lg shadow-lg w-full max-w-lg p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium">{{ editingId ? 'Edit Tag' : 'Create Tag' }}</h3>
            <button @click="closeForm" class="text-gray-500 hover:text-gray-700">Close</button>
          </div>

          <div class="grid grid-cols-1 gap-3">
            <label>
              <div class="text-sm text-slate-600">Name</div>
              <input v-model="form.name" class="w-full border rounded px-3 py-2" />
            </label>
            <label>
              <div class="text-sm text-slate-600">Description</div>
              <textarea v-model="form.description" class="w-full border rounded px-3 py-2" rows="3"></textarea>
            </label>
          </div>

          <div class="mt-4 flex justify-end gap-2">
            <button @click="closeForm" class="px-3 py-2 border rounded">Cancel</button>
            <button @click="saveTag" class="bg-blue-600 text-white px-3 py-2 rounded">Save</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const api = useApi()
const auth = useAuth()

const tags = ref([])
const loading = ref(false)
const error = ref(null)

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ name: '', description: '' })

const load = async () => {
  loading.value = true
  error.value = null
  try {
    const resp = await api.get('/tags')
    tags.value = resp.data || []
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Error loading tags' }
  finally { loading.value = false }
}

const openCreate = () => { editingId.value = null; form.value = { name: '', description: '' }; showForm.value = true }
const openEdit = (t) => { editingId.value = t.id; form.value = { name: t.name, description: t.description }; showForm.value = true }
const closeForm = () => { showForm.value = false }

const saveTag = async () => {
  error.value = null
  try {
    if (editingId.value) {
      await api.put(`/tags/${editingId.value}`, { name: form.value.name, description: form.value.description })
    } else {
      await api.post('/tags', { name: form.value.name, description: form.value.description })
    }
    await load()
    closeForm()
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Error saving tag' }
}

const removeTag = async (t) => {
  if (!confirm(`Remove tag ${t.name}?`)) return
  try { await api.delete(`/tags/${t.id}`); await load() } catch (e) { console.error(e); alert(e?.response?.data?.message || 'Error removing tag') }
}

onMounted(() => {
  // require RESPONSAVEL or ADMIN to manage tags
  if (!auth.user.value || (auth.user.value.role !== 'RESPONSAVEL' && auth.user.value.role !== 'ADMINISTRADOR')) {
    return navigateTo('/')
  }
  load()
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity .15s ease }
.fade-enter-from, .fade-leave-to { opacity: 0 }
</style>
