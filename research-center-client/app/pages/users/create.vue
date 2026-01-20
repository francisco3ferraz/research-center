<template>
  <div class="max-w-2xl mx-auto">
    <div class="md:flex md:items-center md:justify-between mb-6">
      <div class="flex-1 min-w-0">
        <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">Create New User</h2>
      </div>
    </div>

    <div class="bg-white shadow sm:rounded-lg">
      <div class="px-4 py-5 sm:p-6">
        <form @submit.prevent="createUser" class="space-y-6">
          <div>
            <label for="username" class="block text-sm font-medium text-gray-700">Username</label>
            <div class="mt-1">
              <input type="text" v-model="form.username" id="username" required class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border">
            </div>
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700">Password</label>
            <div class="mt-1">
              <input type="password" v-model="form.password" id="password" required class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border">
            </div>
          </div>

          <div>
            <label for="name" class="block text-sm font-medium text-gray-700">Name</label>
            <div class="mt-1">
              <input type="text" v-model="form.name" id="name" required class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border">
            </div>
          </div>

          <div>
            <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
            <div class="mt-1">
              <input type="email" v-model="form.email" id="email" required class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border">
            </div>
          </div>

          <div>
            <label for="role" class="block text-sm font-medium text-gray-700">Role</label>
            <div class="mt-1">
              <select id="role" v-model="form.role" class="shadow-sm focus:ring-blue-500 focus:border-blue-500 block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border">
                <option value="COLABORADOR">Collaborator</option>
                <option value="RESPONSAVEL">Manager</option>
                <option value="ADMINISTRADOR">Administrator</option>
              </select>
            </div>
          </div>

          <div class="flex justify-end">
            <NuxtLink to="/users" class="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 mr-3">
              Cancel
            </NuxtLink>
            <button type="submit" :disabled="loading" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
              {{ loading ? 'Creating...' : 'Create User' }}
            </button>
          </div>
          <div v-if="error" class="text-red-500 text-sm mt-2">{{ error }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
const config = useRuntimeConfig()
const auth = useAuth()
const router = useRouter()

// Initialize auth and check login
onMounted(() => {
    auth.initAuth()
    if (!auth.token.value) {
        navigateTo('/auth/login')
    }
})

const form = reactive({
    username: '',
    password: '',
    name: '',
    email: '',
    role: 'COLABORADOR'
})
const loading = ref(false)
const error = ref(null)

const createUser = async () => {
    loading.value = true
    error.value = null
    try {
        await $fetch(`${config.public.apiBase}/users`, {
            method: 'POST',
            body: form,
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        router.push('/users')
    } catch (e) {
        error.value = "Error creating user: " + (e.data?.message || e.message)
    } finally {
        loading.value = false
    }
}
</script>
