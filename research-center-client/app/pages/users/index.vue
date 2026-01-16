<template>
  <div>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-bold text-gray-900">Utilizadores</h2>
      <NuxtLink to="/users/create" class="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-sm font-medium">
        + Novo Utilizador
      </NuxtLink>
    </div>

    <div class="flex flex-col">
      <div class="-my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
        <div class="py-2 align-middle inline-block min-w-full sm:px-6 lg:px-8">
          <div class="shadow overflow-hidden border-b border-gray-200 sm:rounded-lg">
            <div v-if="pending" class="p-4 text-center">Carregando...</div>
            <div v-else-if="error" class="p-4 text-center text-red-500">Erro ao carregar utilizadores: {{ error }}</div>
            <table v-else class="min-w-full divide-y divide-gray-200">
              <thead class="bg-gray-50">
                <tr>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Username</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Nome</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                  <th scope="col" class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                  <th scope="col" class="relative px-6 py-3">
                    <span class="sr-only">Ações</span>
                  </th>
                </tr>
              </thead>
              <tbody class="bg-white divide-y divide-gray-200">
                <tr v-for="user in users" :key="user.id">
                  <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ user.username }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.name }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ user.email }}</td>
                  <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                        {{ user.role || 'N/A' }}
                      </span>
                  </td>
                  <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <NuxtLink :to="`/users/${user.id}`" class="text-blue-600 hover:text-blue-900 mr-4">Detalhes</NuxtLink>
                    <button @click="deleteUser(user.id)" class="text-red-600 hover:text-red-900">Apagar</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const config = useRuntimeConfig()
const auth = useAuth()

// State for users list
const users = ref([])
const pending = ref(true)
const error = ref(null)

// Only fetch on client side
onMounted(async () => {
    // Initialize auth from localStorage
    auth.initAuth()
    
    // Redirect to login if no token
    if (!auth.token.value) {
        navigateTo('/auth/login')
        return
    }
    
    try {
        const data = await $fetch(`${config.public.apiBase}/users`, {
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        users.value = data
    } catch (e) {
        console.error('Error fetching users:', e)
        error.value = e.data?.message || e.message || 'Erro ao carregar utilizadores'
    } finally {
        pending.value = false
    }
})

const refresh = async () => {
    pending.value = true
    error.value = null
    try {
        const data = await $fetch(`${config.public.apiBase}/users`, {
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        users.value = data
    } catch (e) {
        error.value = e.data?.message || e.message
    } finally {
        pending.value = false
    }
}

const deleteUser = async (id) => {
    if (!confirm(`Tem a certeza que deseja apagar este utilizador?`)) return

    try {
        await $fetch(`${config.public.apiBase}/users/${id}`, {
            method: 'DELETE',
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        refresh() // Reload list
    } catch (e) {
        alert('Erro ao apagar utilizador: ' + (e.data?.message || e.message))
    }
}
</script>

