<template>
  <div class="flex min-h-full flex-col justify-center px-6 py-12 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-sm">
      <h2 class="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">Recuperar Password</h2>
      <p class="mt-2 text-center text-sm text-gray-500">
        Introduza o seu email para receber instruções de recuperação.
      </p>
    </div>

    <div class="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
      <form class="space-y-6" @submit.prevent="handleSubmit">
        <div>
          <label for="email" class="block text-sm font-medium leading-6 text-gray-900">Email</label>
          <div class="mt-2">
            <input id="email" v-model="email" name="email" type="email" autocomplete="email" required class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3">
          </div>
        </div>

        <div>
          <button type="submit" :disabled="loading" class="flex w-full justify-center rounded-md bg-blue-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:opacity-50">
            {{ loading ? 'A enviar...' : 'Enviar Email' }}
          </button>
        </div>
        
        <div v-if="message" class="text-green-600 text-sm text-center bg-green-50 p-2 rounded">
            {{ message }}
        </div>
      </form>

      <p class="mt-10 text-center text-sm text-gray-500">
        <NuxtLink to="/auth/login" class="font-semibold leading-6 text-blue-600 hover:text-blue-500">Voltar ao Login</NuxtLink>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const email = ref('')
const loading = ref(false)
const message = ref('')

const handleSubmit = () => {
    loading.value = true
    // Simulating backend call
    setTimeout(() => {
        loading.value = false
        message.value = 'Funcionalidade será implementada brevemente. (Se o email existisse, receberia instruções).'
        email.value = ''
    }, 1000)
}
</script>
