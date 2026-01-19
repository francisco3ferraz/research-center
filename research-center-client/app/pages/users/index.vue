<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
        <div>
          <h2 class="text-2xl font-semibold">Gestão de Utilizadores</h2>
          <p class="text-sm text-slate-500 mt-1">Ver, criar e gerir contas de utilizador</p>
        </div>

        <div class="flex items-center gap-3">
          <div class="relative">
            <input v-model="search" placeholder="Procurar por nome, username ou email" class="border rounded-md pl-10 pr-4 py-2 w-64 focus:ring-2 focus:ring-blue-200" />
            <svg class="w-4 h-4 absolute left-3 top-2.5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-4.35-4.35M17 11a6 6 0 11-12 0 6 6 0 0112 0z"/></svg>
          </div>
          <button @click="openCreate" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">+ Novo Utilizador</button>
        </div>
      </div>

      <div v-if="loading" class="text-slate-600">A carregar utilizadores...</div>

      <div v-else>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nome</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Papel</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Estado</th>
                <th class="px-6 py-3"></th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr v-for="u in filteredUsers" :key="u.id" class="hover:bg-gray-50">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ u.name }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ u.username }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ u.email }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">{{ u.role || 'N/A' }}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span :class="u.active ? 'text-green-700' : 'text-red-600'">{{ u.active ? 'Ativo' : 'Inativo' }}</span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button @click="openEdit(u)" title="Editar" class="text-blue-600 hover:text-blue-900 mr-3">Editar</button>
                  <NuxtLink :to="`/users/${u.id}/activity`" class="text-purple-600 hover:text-purple-900 mr-3" title="Histórico">Histórico</NuxtLink>
                  <button @click="toggleActive(u)" title="Ativar/Desativar" class="text-gray-600 hover:text-gray-900 mr-3">{{ u.active ? 'Desativar' : 'Ativar' }}</button>
                  <button @click="removeUser(u)" title="Remover" class="text-red-600 hover:text-red-900">Remover</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div v-if="error" class="text-red-600 mt-3">{{ error }}</div>
    </div>

    <!-- Modal: create / edit -->
    <transition name="fade">
      <div v-if="showForm" class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-40 z-40">
        <div class="bg-white rounded-lg shadow-lg w-full max-w-xl p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-medium">{{ editingId ? 'Editar Utilizador' : 'Criar Utilizador' }}</h3>
            <button @click="closeForm" class="text-gray-500 hover:text-gray-700">Fechar</button>
          </div>

          <div class="grid grid-cols-1 gap-3">
            <label>
              <div class="text-sm text-slate-600">Nome</div>
              <input v-model="form.name" class="w-full border rounded px-3 py-2" />
            </label>
            <label>
              <div class="text-sm text-slate-600">Username</div>
              <input v-model="form.username" class="w-full border rounded px-3 py-2" :disabled="editingId" />
            </label>
            <label>
              <div class="text-sm text-slate-600">Email</div>
              <input v-model="form.email" class="w-full border rounded px-3 py-2" />
            </label>
            <label>
              <div class="text-sm text-slate-600">Papel</div>
              <select v-model="form.role" class="w-full border rounded px-3 py-2">
                <option value="COLABORADOR">COLABORADOR</option>
                <option value="RESPONSAVEL">RESPONSAVEL</option>
                <option value="ADMINISTRADOR">ADMINISTRADOR</option>
              </select>
            </label>
            <label v-if="!editingId">
              <div class="text-sm text-slate-600">Password</div>
              <input v-model="form.password" type="password" class="w-full border rounded px-3 py-2" />
            </label>
          </div>

          <div class="mt-4 flex justify-end gap-2">
            <button @click="closeForm" class="px-3 py-2 border rounded">Cancelar</button>
            <button @click="saveUser" class="bg-blue-600 text-white px-3 py-2 rounded">Guardar</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
const api = useApi()
const auth = useAuth()

const users = ref([])
const loading = ref(false)
const error = ref(null)

const search = ref('')
const showForm = ref(false)
const editingId = ref(null)
const form = ref({ username: '', name: '', email: '', password: '', role: 'COLABORADOR' })

const filteredUsers = computed(() => {
  const q = (search.value || '').toLowerCase().trim()
  if (!q) return users.value
  return users.value.filter(u => {
    return (u.name || '').toLowerCase().includes(q)
      || (u.username || '').toLowerCase().includes(q)
      || (u.email || '').toLowerCase().includes(q)
  })
})

const loadUsers = async () => {
  loading.value = true
  error.value = null
  try {
    const resp = await api.get('/users')
    users.value = resp.data || []
  } catch (e) {
    console.error(e)
    error.value = e?.response?.data?.message || 'Erro ao carregar utilizadores'
  } finally { loading.value = false }
}

const openCreate = () => {
  editingId.value = null
  form.value = { username: '', name: '', email: '', password: '', role: 'COLABORADOR' }
  showForm.value = true
}

const openEdit = (u) => {
  editingId.value = u.id
  form.value = { username: u.username, name: u.name, email: u.email, password: '', role: u.role }
  showForm.value = true
}

const closeForm = () => { showForm.value = false }

const saveUser = async () => {
  error.value = null
  try {
    if (editingId.value) {
      await api.put(`/users/${editingId.value}`, {
        name: form.value.name,
        email: form.value.email,
        role: form.value.role
      })
    } else {
      await api.post('/users', {
        username: form.value.username,
        password: form.value.password,
        name: form.value.name,
        email: form.value.email,
        role: form.value.role
      })
    }
    await loadUsers()
    closeForm()
  } catch (e) {
    console.error(e)
    error.value = e?.response?.data?.message || 'Erro ao guardar utilizador'
  }
}

const removeUser = async (u) => {
  if (!confirm(`Remover utilizador ${u.name}?`)) return
  try {
    await api.delete(`/users/${u.id}`)
    await loadUsers()
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Erro ao remover utilizador' }
}

const toggleActive = async (u) => {
  try {
    await api.patch(`/users/${u.id}/status`, { active: !u.active })
    await loadUsers()
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Erro ao alterar estado' }
}

onMounted(() => {
  // Only admins should access this page; the layout already hides link, but protect here too
  if (!auth.user.value || auth.user.value.role !== 'ADMINISTRADOR') {
    return navigateTo('/')
  }
  loadUsers()
})
</script>

<style scoped>
.table-auto th, .table-auto td { padding: 0.5rem 0.75rem; }
</style>

<!-- Duplicate template/script removed; kept the first component implementation above -->

