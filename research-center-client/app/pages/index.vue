<template>
  <div class="bg-white shadow overflow-hidden sm:rounded-lg">
    <div class="px-4 py-5 sm:px-6">
      <h3 class="text-lg leading-6 font-medium text-gray-900">Research Center Management</h3>
      <p class="mt-1 max-w-2xl text-sm text-gray-500">Bem-vindo à aplicação de gestão.</p>
    </div>
    <div class="border-t border-gray-200 px-4 py-5 sm:p-0">
      <div v-if="!auth.token.value" class="p-4">
        <p class="mb-4">Por favor, faça login para aceder às funcionalidades.</p>
        <NuxtLink to="/auth/login" class="text-blue-600 hover:text-blue-800 font-bold">Ir para Login &rarr;</NuxtLink>
      </div>
      <div v-else class="p-4">
        <p class="mb-4">Sessão iniciada como <strong>{{ auth.user.value?.username || auth.user.value?.sub }}</strong>.</p>
        <NuxtLink to="/users" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Gerir Utilizadores</NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup>
const auth = useAuth()
</script>
