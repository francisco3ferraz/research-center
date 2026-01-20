<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-6">
      <h1 class="text-2xl font-bold text-gray-900">Notificações</h1>
      <button 
        v-if="notifications.length > 0"
        @click="markAllAsRead"
        :disabled="loading"
        class="text-sm text-blue-600 hover:text-blue-500"
      >
        Marcar todas como lidas
      </button>
    </div>

    <!-- Loading State -->
    <div v-if="loading && notifications.length === 0" class="text-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      <p class="mt-2 text-gray-500">A carregar notificações...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="notifications.length === 0" class="text-center py-12 bg-gray-50 rounded-lg">
      <div class="text-gray-400 text-5xl mb-4">🔔</div>
      <h3 class="text-lg font-medium text-gray-900">Sem notificações</h3>
      <p class="mt-1 text-gray-500">Não tem notificações por ler.</p>
    </div>

    <!-- Notifications List -->
    <div v-else class="space-y-4">
      <div 
        v-for="notification in notifications" 
        :key="notification.id"
        class="bg-white rounded-lg shadow p-4 border-l-4 transition-all"
        :class="notification.read ? 'border-gray-200 opacity-60' : 'border-blue-500'"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1">
            <div class="flex items-center gap-2">
              <span class="text-lg">{{ getNotificationIcon(notification.type) }}</span>
              <h3 class="font-medium text-gray-900">{{ notification.title }}</h3>
              <span v-if="!notification.read" class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800">
                Nova
              </span>
            </div>
            <p class="mt-1 text-sm text-gray-600">{{ notification.message }}</p>
            <p class="mt-2 text-xs text-gray-400">{{ formatDate(notification.createdAt) }}</p>
          </div>
          <div class="flex gap-2 ml-4">
            <button 
              v-if="!notification.read"
              @click="markAsRead(notification.id)"
              class="text-sm text-blue-600 hover:text-blue-500"
              title="Marcar como lida"
            >
              ✓
            </button>
            <button 
              @click="deleteNotification(notification.id)"
              class="text-sm text-red-600 hover:text-red-500"
              title="Remover"
            >
              🗑️
            </button>
          </div>
        </div>
        
        <!-- Link to related content -->
        <div v-if="notification.relatedEntityType && notification.relatedEntityId" class="mt-3">
          <NuxtLink 
            v-if="notification.relatedEntityType === 'PUBLICATION'"
            :to="`/publications/${notification.relatedEntityId}`"
            class="text-sm text-blue-600 hover:underline"
          >
            Ver publicação →
          </NuxtLink>
        </div>
      </div>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="mt-4 p-4 bg-red-50 text-red-600 rounded-lg">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
definePageMeta({
  middleware: ['auth']
})

const api = useApi()
const notifications = ref([])
const loading = ref(true)
const error = ref(null)

const fetchNotifications = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await api.get('/notifications')
    notifications.value = response.data || []
  } catch (e) {
    console.error('Failed to fetch notifications', e)
    error.value = 'Erro ao carregar notificações.'
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id) => {
  try {
    await api.patch(`/notifications/${id}/read`)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.read = true
    }
  } catch (e) {
    console.error('Failed to mark as read', e)
  }
}

const markAllAsRead = async () => {
  try {
    for (const n of notifications.value.filter(n => !n.read)) {
      await api.patch(`/notifications/${n.id}/read`)
      n.read = true
    }
  } catch (e) {
    console.error('Failed to mark all as read', e)
  }
}

const deleteNotification = async (id) => {
  try {
    await api.delete(`/notifications/${id}`)
    notifications.value = notifications.value.filter(n => n.id !== id)
  } catch (e) {
    console.error('Failed to delete notification', e)
  }
}

const getNotificationIcon = (type) => {
  const icons = {
    'NEW_PUBLICATION_WITH_TAG': '📄',
    'NEW_COMMENT_ON_TAG': '💬',
    'NEW_RATING': '⭐',
    'SYSTEM': '🔔'
  }
  return icons[type] || '🔔'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('pt-PT', { 
    day: '2-digit', 
    month: 'short', 
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchNotifications()
})
</script>
