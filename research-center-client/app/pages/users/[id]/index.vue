<template>
  <div class="max-w-2xl mx-auto">
    <div v-if="pending" class="text-center py-10">Loading...</div>
    <div v-else-if="errorLoad" class="text-center py-10 text-red-500">Error loading details: {{ errorLoad }}</div>
    
    <div v-else>
      <div class="md:flex md:items-center md:justify-between mb-6">
        <div class="flex-1 min-w-0">
          <h2 class="text-2xl font-bold leading-7 text-gray-900 sm:text-3xl sm:truncate">User Details: {{ user?.username }}</h2>
        </div>
      </div>

      <div class="bg-white shadow sm:rounded-lg">
        <div class="px-4 py-5 sm:p-6">
          <form @submit.prevent="updateUser" class="space-y-6">
            <!-- Username/ID is typically read-only -->
            <div>
              <label class="block text-sm font-medium text-gray-700">Username</label>
              <div class="mt-1">
                <input type="text" :value="user?.username" disabled class="bg-gray-100 shadow-sm block w-full sm:text-sm border-gray-300 rounded-md px-3 py-2 border cursor-not-allowed">
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

            <div class="flex justify-end pt-4 border-t">
              <NuxtLink to="/users" class="bg-white py-2 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 mr-3">
                Back
              </NuxtLink>
              <button type="submit" :disabled="saving" class="inline-flex justify-center py-2 px-4 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500">
                {{ saving ? 'Saving...' : 'Save Changes' }}
              </button>
            </div>
            <div v-if="errorSave" class="text-red-500 text-sm mt-2">{{ errorSave }}</div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const route = useRoute()
const config = useRuntimeConfig()
const auth = useAuth()
const router = useRouter()

const id = route.params.id
const saving = ref(false)
const errorSave = ref(null)

// State for user data
const user = ref(null)
const pending = ref(true)
const errorLoad = ref(null)

const form = reactive({
    name: '',
    email: '',
    role: ''
})

// Fetch user on mount (client-side only)
onMounted(async () => {
    // Rely on plugin/layout for auth init, or check here
    if (!auth.token.value) {
        navigateTo('/auth/login')
        return
    }
    
    try {
        const data = await $fetch(`${config.public.apiBase}/users/${id}`, {
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        user.value = data
        // Populate form
        form.name = data.name
        form.email = data.email
        form.role = data.role
    } catch (e) {
        errorLoad.value = e.data?.message || e.message || 'Error loading user'
    } finally {
        pending.value = false
    }
})

const updateUser = async () => {
    saving.value = true
    errorSave.value = null
    try {
        await $fetch(`${config.public.apiBase}/users/${id}`, {
            method: 'PUT',
            body: form,
            headers: {
                Authorization: `Bearer ${auth.token.value}`
            }
        })
        alert('User updated successfully!')
        router.push('/users')
    } catch (e) {
        errorSave.value = "Error updating: " + (e.data?.message || e.message)
    } finally {
        saving.value = false
    }
}
</script>
