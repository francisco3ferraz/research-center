<template>
  <div class="flex min-h-full flex-col justify-center px-6 py-12 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-sm">
      <h2 class="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">Sign in to your account</h2>
    </div>

    <div class="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
      <form class="space-y-6" @submit.prevent="handleLogin">
        <div>
          <label for="username" class="block text-sm font-medium leading-6 text-gray-900">Username</label>
          <div class="mt-2">
            <input id="username" v-model="form.username" name="username" type="text" required class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3">
          </div>
        </div>

        <div>
          <div class="flex items-center justify-between">
            <label for="password" class="block text-sm font-medium leading-6 text-gray-900">Password</label>
            <div class="text-sm">
              <NuxtLink to="/auth/forgot-password" class="font-semibold text-blue-600 hover:text-blue-500">Forgot password?</NuxtLink>
            </div>
          </div>
          <div class="mt-2">
            <input id="password" v-model="form.password" name="password" type="password" required class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3">
          </div>
        </div>

        <div>
          <button type="submit" :disabled="loading" class="flex w-full justify-center rounded-md bg-blue-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:opacity-50">
            {{ loading ? 'Signing in...' : 'Sign in' }}
          </button>
        </div>
        
        <div v-if="error" class="text-red-500 text-sm text-center">
            {{ error }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
const form = reactive({
    username: '',
    password: ''
})
const loading = ref(false)
const error = ref(null)

const config = useRuntimeConfig()
const auth = useAuth()

const handleLogin = async () => {
    loading.value = true
    error.value = null
    try {
        // 1. Get Token (backend returns { token: "jwt_string" })
        const response = await $fetch(`${config.public.apiBase}/auth/login`, {
            method: 'POST',
            body: form
        })
        
        // Extract the token string from the response object
        const token = response?.token || response
        
        if (token) {
            // 2. We have a token. Now let's try to get user details if possible.
            let user = { username: form.username } // Default fallback
            
            try {
                const userData = await $fetch(`${config.public.apiBase}/auth/user`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                })
                if (userData) {
                    user = userData
                }
            } catch (userErr) {
                console.warn('Could not fetch user details', userErr)
            }

            auth.login(token, user)
            navigateTo('/users')
        }
    } catch (e) {
        console.error(e)
        error.value = "Login failed. Check your credentials."
    } finally {
        loading.value = false
    }
}
</script>
