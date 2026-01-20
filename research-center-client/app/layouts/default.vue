<template>
  <div class="min-h-screen bg-gray-100 font-sans">
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <h1 class="text-xl font-bold text-blue-600">Research Center</h1>
            </div>
            <div class="hidden sm:ml-6 sm:flex sm:space-x-8 items-center">
              <NuxtLink
                to="/"
                class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Home
              </NuxtLink>

              <!-- Publication links for authenticated users -->
              <NuxtLink
                v-if="auth.token.value"
                to="/publications/my"
                class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Minhas Publicações
              </NuxtLink>

              <NuxtLink
                v-if="auth.token.value"
                to="/subscriptions"
                class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Subscrições
              </NuxtLink>

              <NuxtLink
                v-if="auth.token.value"
                to="/notifications"
                class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                🔔 Notificações
              </NuxtLink>

            </div>
          </div>
          <div class="flex items-center">
            <template v-if="auth.token.value">
              <div
                v-if="
                  auth.token.value &&
                  (auth.user.value?.role === 'ADMINISTRADOR' ||
                    auth.user.value?.role === 'RESPONSAVEL')
                "
                class="relative mr-4"
              >
                <button
                  @click="adminOpen = !adminOpen"
                  class="bg-gray-100 text-gray-700 hover:bg-gray-200 px-3 py-2 rounded-md text-sm font-medium inline-flex items-center"
                >
                  {{ auth.user.value?.role === 'ADMINISTRADOR' ? 'Administrador' : 'Responsável' }}
                  <svg class="ml-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                </button>

                <div
                  v-if="adminOpen"
                  class="absolute right-0 mt-2 w-48 bg-white shadow-lg rounded-md ring-1 ring-black ring-opacity-5 z-20 origin-top-right"
                >
                  <div class="py-1">
                    <NuxtLink
                      v-if="auth.user.value?.role === 'ADMINISTRADOR'"
                      to="/users"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Gerir Utilizadores</NuxtLink>
                    <NuxtLink
                      to="/tags"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Gerir Tags</NuxtLink>
                    <NuxtLink
                      v-if="auth.user.value?.role === 'ADMINISTRADOR'"
                      to="/scientific-areas"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Gerir Áreas Científicas</NuxtLink>
                    <NuxtLink
                      to="/admin/hidden"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Conteúdo Oculto</NuxtLink>
                  </div>
                </div>
              </div>

              <NuxtLink
                to="/profile"
                class="text-sm text-gray-500 mr-4 hover:text-gray-700"
                >Perfil</NuxtLink
              >
              <button
                @click="handleLogout"
                class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
              >
                Logout
              </button>
            </template>
            <template v-else>
              <NuxtLink
                to="/auth/login"
                class="text-gray-500 hover:text-gray-700 px-3 py-2 rounded-md text-sm font-medium"
              >
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
const auth = useAuth();

onMounted(() => {
  auth.initAuth();
});

const handleLogout = () => {
  auth.logout();
};
import { ref } from "vue";

const adminOpen = ref(false);
</script>
