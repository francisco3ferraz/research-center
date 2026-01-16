<template>
  <div class="min-h-screen bg-gray-100 font-sans">
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-blue-600">Research Center</h1>
            </div>
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
              <NuxtLink to="/" class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                Home
              </NuxtLink>
              <NuxtLink v-if="auth.token.value" to="/users" class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium">
                Utilizadores
              </NuxtLink>
            </div>
          </div>
          <div class="flex items-center">
            <template v-if="auth.token.value">
              <span class="text-sm text-gray-500 mr-4">User: {{ auth.user.value?.username || auth.user.value?.sub || 'Auth' }}</span>
              <button @click="handleLogout" class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium">
                Logout
              </button>
            </template>
            <template v-else>
              <NuxtLink to="/auth/login" class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium">
                Login
              </NuxtLink>
            </template>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <slot />
    </main>
  </div>
</template>

<script setup>
const auth = useAuth()

onMounted(() => {
    auth.initAuth()
})

const handleLogout = () => {
    auth.logout()
}
</script>
