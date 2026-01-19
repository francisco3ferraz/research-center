<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-2xl font-semibold">Áreas Científicas</h2>
          <p class="text-sm text-slate-500">Gerir áreas científicas (só ADMIN)</p>
        </div>
        <div>
          <button @click="openCreate" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">+ Nova Área</button>
        </div>
      </div>

      <div v-if="loading" class="text-slate-600">A carregar áreas...</div>

      <div v-else>
        <ul class="space-y-2">
          <li v-for="a in areas" :key="a.id" class="flex items-center justify-between p-3 border rounded-md hover:bg-gray-50">
            <div>
              <div class="font-medium text-gray-900">{{ a.name }}</div>
              <div class="text-sm text-slate-500">{{ a.description }}</div>
            </div>
            <div class="flex items-center gap-3">
              <button @click="openEdit(a)" class="text-blue-600 hover:text-blue-900">Editar</button>
              <button @click="removeArea(a)" class="text-red-600 hover:text-red-800">Remover</button>
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
            <h3 class="text-lg font-medium">{{ editingId ? 'Editar Área' : 'Criar Área' }}</h3>
            <button @click="closeForm" class="text-gray-500 hover:text-gray-700">Fechar</button>
          </div>

          <div class="grid grid-cols-1 gap-3">
            <label>
              <div class="text-sm text-slate-600">Nome</div>
              <input v-model="form.name" class="w-full border rounded px-3 py-2" />
            </label>
            <label>
              <div class="text-sm text-slate-600">Descrição</div>
              <textarea v-model="form.description" class="w-full border rounded px-3 py-2" rows="3"></textarea>
            </label>
          </div>

          <div class="mt-4 flex justify-end gap-2">
            <button @click="closeForm" class="px-3 py-2 border rounded">Cancelar</button>
            <button @click="saveArea" class="bg-blue-600 text-white px-3 py-2 rounded">Guardar</button>
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

const areas = ref([])
const loading = ref(false)
const error = ref(null)

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ name: '', description: '' })

const load = async () => {
  loading.value = true
  error.value = null
  try {
    const resp = await api.get('/scientific-areas')
    areas.value = resp.data || []
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Erro ao carregar áreas' }
  finally { loading.value = false }
}

const openCreate = () => { editingId.value = null; form.value = { name: '', description: '' }; showForm.value = true }
const openEdit = (a) => { editingId.value = a.id; form.value = { name: a.name, description: a.description }; showForm.value = true }
const closeForm = () => { showForm.value = false }

const saveArea = async () => {
  error.value = null
  try {
    if (editingId.value) {
      await api.put(`/scientific-areas/${editingId.value}`, { name: form.value.name, description: form.value.description })
    } else {
      await api.post('/scientific-areas', { name: form.value.name, description: form.value.description })
    }
    await load()
    closeForm()
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Erro ao guardar área' }
}

const removeArea = async (a) => {
  if (!confirm(`Remover área ${a.name}?`)) return
  try { await api.delete(`/scientific-areas/${a.id}`); await load() } catch (e) { console.error(e); alert(e?.response?.data?.message || 'Erro ao remover área') }
}

onMounted(() => {
  // only ADMIN can manage scientific areas
  if (!auth.user.value || auth.user.value.role !== 'ADMINISTRADOR') {
    return navigateTo('/')
  }
  load()
})
</script>

<style scoped>
.fade-enter-active, .fade-leave-active { transition: opacity .15s ease }
.fade-enter-from, .fade-leave-to { opacity: 0 }
</style>
