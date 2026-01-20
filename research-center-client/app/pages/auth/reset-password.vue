<template>
  <div class="flex min-h-full flex-col justify-center px-6 py-12 lg:px-8">
    <div class="sm:mx-auto sm:w-full sm:max-w-sm">
      <h2 class="mt-10 text-center text-2xl font-bold leading-9 tracking-tight text-gray-900">Reset Password</h2>
      <p class="mt-2 text-center text-sm text-gray-500">
        Enter your new password.
      </p>
    </div>

    <div class="mt-10 sm:mx-auto sm:w-full sm:max-w-sm">
      <!-- Token Validation -->
      <div v-if="validating" class="text-center">
        <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p class="mt-2 text-gray-500">Validating token...</p>
      </div>

      <!-- Invalid Token Message -->
      <div v-else-if="!tokenValid" class="text-center">
        <div class="text-red-500 text-6xl mb-4">⚠️</div>
        <h3 class="text-lg font-semibold text-gray-900">Invalid or Expired Token</h3>
        <p class="mt-2 text-gray-500">The recovery link is invalid or has expired.</p>
        <NuxtLink to="/auth/forgot-password" class="mt-4 inline-block text-blue-600 hover:text-blue-500">
          Request new recovery link
        </NuxtLink>
      </div>

      <!-- Reset Form -->
      <form v-else class="space-y-6" @submit.prevent="handleSubmit">
        <div>
          <label for="password" class="block text-sm font-medium leading-6 text-gray-900">New Password</label>
          <div class="mt-2">
            <input id="password" v-model="form.newPassword" name="password" type="password" required minlength="6" class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3">
          </div>
        </div>

        <div>
          <label for="confirmPassword" class="block text-sm font-medium leading-6 text-gray-900">Confirm Password</label>
          <div class="mt-2">
            <input id="confirmPassword" v-model="form.confirmPassword" name="confirmPassword" type="password" required minlength="6" class="block w-full rounded-md border-0 py-1.5 text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6 px-3">
          </div>
        </div>

        <div v-if="form.newPassword && form.confirmPassword && form.newPassword !== form.confirmPassword" class="text-red-500 text-sm">
          Passwords do not match.
        </div>

        <div>
          <button type="submit" :disabled="loading || form.newPassword !== form.confirmPassword" class="flex w-full justify-center rounded-md bg-blue-600 px-3 py-1.5 text-sm font-semibold leading-6 text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 disabled:opacity-50">
            {{ loading ? 'Resetting...' : 'Reset Password' }}
          </button>
        </div>
        
        <div v-if="message" class="text-sm text-center p-3 rounded" :class="success ? 'text-green-600 bg-green-50' : 'text-red-600 bg-red-50'">
          {{ message }}
        </div>
      </form>

      <!-- Success State -->
      <div v-if="success" class="mt-6 text-center">
        <NuxtLink to="/auth/login" class="font-semibold text-blue-600 hover:text-blue-500">
          Go to Login
        </NuxtLink>
      </div>
    </div>
  </div>
</template>

<script setup>
const route = useRoute()
const config = useRuntimeConfig()

const token = computed(() => route.query.token || '')
const validating = ref(true)
const tokenValid = ref(false)
const loading = ref(false)
const message = ref('')
const success = ref(false)

const form = reactive({
  newPassword: '',
  confirmPassword: ''
})

// Validate token on mount
onMounted(async () => {
  if (!token.value) {
    validating.value = false
    tokenValid.value = false
    return
  }

  try {
    const response = await $fetch(`${config.public.apiBase}/auth/validate-token?token=${token.value}`)
    tokenValid.value = response?.valid === true
  } catch (e) {
    console.error('Token validation failed', e)
    tokenValid.value = false
  } finally {
    validating.value = false
  }
})

const handleSubmit = async () => {
  if (form.newPassword !== form.confirmPassword) {
    message.value = 'Passwords do not match.'
    return
  }

  loading.value = true
  message.value = ''
  success.value = false

  try {
    await $fetch(`${config.public.apiBase}/auth/reset-password`, {
      method: 'POST',
      body: {
        token: token.value,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword
      }
    })

    success.value = true
    message.value = 'Password reset successfully! You can login with your new password.'
    form.newPassword = ''
    form.confirmPassword = ''
  } catch (e) {
    console.error(e)
    message.value = e?.data?.message || 'An error occurred. The token may have expired.'
  } finally {
    loading.value = false
  }
}
</script>
