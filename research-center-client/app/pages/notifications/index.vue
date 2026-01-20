<template>
  <div class="px-4 sm:px-6 lg:px-8 py-8">
    <div class="max-w-4xl mx-auto">
      <!-- Header -->
      <div class="mb-6">
        <h1 class="text-3xl font-bold text-gray-900">Notifications</h1>
        <p class="mt-2 text-sm text-gray-600">
          Manage all your notifications
        </p>
      </div>

      <!-- Actions Bar -->
      <div class="mb-6 flex justify-between items-center">
        <div class="flex gap-4">
          <button
            @click="filterUnread = !filterUnread"
            :class="[
              'px-4 py-2 rounded-md text-sm font-medium transition-colors',
              filterUnread
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            ]"
          >
            {{ filterUnread ? 'Show All' : 'Unread Only' }}
          </button>
        </div>
        <button
          v-if="notificationsState.unreadCount.value > 0"
          @click="handleMarkAllAsRead"
          class="px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
        >
          Mark All as Read
        </button>
      </div>

      <!-- Loading State -->
      <div v-if="notificationsState.loading.value" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-4 text-gray-600">Loading notifications...</p>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="displayedNotifications.length === 0"
        class="text-center py-12 bg-white rounded-lg shadow"
      >
        <div class="text-6xl mb-4">🔔</div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">
          {{ filterUnread ? 'No unread notifications' : 'No notifications' }}
        </h3>
        <p class="text-gray-600">
          {{ filterUnread ? 'All your notifications have been read.' : 'You have no notifications at the moment.' }}
        </p>
      </div>

      <!-- Notifications List -->
      <div v-else class="space-y-4">
        <div
          v-for="notification in displayedNotifications"
          :key="notification.id"
          class="bg-white rounded-lg shadow hover:shadow-md transition-shadow"
          :class="{ 'border-l-4 border-blue-600': !notification.read }"
        >
          <div class="p-6">
            <div class="flex items-start gap-4">
              <!-- Icon -->
              <div class="flex-shrink-0 text-3xl">
                {{ notificationsState.getNotificationIcon(notification.type) }}
              </div>

              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-start justify-between gap-4">
                  <div class="flex-1">
                    <h3 class="text-lg font-semibold text-gray-900 mb-1">
                      {{ notification.title }}
                    </h3>
                    <p class="text-gray-700 mb-3">
                      {{ notification.message }}
                    </p>
                    <div class="flex items-center gap-4 text-sm text-gray-500">
                      <span>{{ notificationsState.formatDate(notification.createdAt) }}</span>
                      <span v-if="notification.read && notification.readAt" class="text-green-600">
                        ✓ Read on {{ notificationsState.formatDate(notification.readAt) }}
                      </span>
                    </div>
                  </div>

                  <!-- Status Badge -->
                  <div v-if="!notification.read" class="flex-shrink-0">
                    <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                      Unread
                    </span>
                  </div>
                </div>

                <!-- Actions -->
                <div class="mt-4 flex items-center gap-3">
                  <!-- Link to related content -->
                  <NuxtLink
                    v-if="notification.relatedEntityType === 'PUBLICATION' && notification.relatedEntityId"
                    :to="`/publications/${notification.relatedEntityId}`"
                    class="inline-flex items-center px-3 py-1.5 border border-blue-600 text-blue-600 rounded-md text-sm font-medium hover:bg-blue-50 transition-colors"
                  >
                    View Publication →
                  </NuxtLink>

                  <!-- Mark as read -->
                  <button
                    v-if="!notification.read"
                    @click="handleMarkAsRead(notification.id)"
                    class="inline-flex items-center px-3 py-1.5 border border-gray-300 text-gray-700 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors"
                  >
                    ✓ Mark as Read
                  </button>

                  <!-- Delete -->
                  <button
                    @click="handleDeleteNotification(notification.id)"
                    class="inline-flex items-center px-3 py-1.5 border border-red-300 text-red-600 rounded-md text-sm font-medium hover:bg-red-50 transition-colors"
                  >
                    🗑️ Remove
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <div v-if="!notificationsState.loading.value && displayedNotifications.length > 0" class="mt-6 text-center text-sm text-gray-600">
        Showing {{ displayedNotifications.length }} of {{ notificationsState.notifications.value.length }} notifications
        <span v-if="notificationsState.unreadCount.value > 0" class="font-medium text-blue-600">
          ({{ notificationsState.unreadCount.value }} unread)
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

definePageMeta({
  middleware: 'auth'
})

const notificationsState = useNotifications()
const filterUnread = ref(false)

onMounted(() => {
  notificationsState.fetchNotifications()
})

const displayedNotifications = computed(() => {
  const notifArray = Array.isArray(notificationsState.notifications.value)
    ? notificationsState.notifications.value
    : []
  
  if (filterUnread.value) {
    return notifArray.filter(n => !n.read)
  }
  return notifArray
})

const handleMarkAsRead = async (id) => {
  await notificationsState.markAsRead(id)
}

const handleMarkAllAsRead = async () => {
  await notificationsState.markAllAsRead()
}

const handleDeleteNotification = async (id) => {
  if (confirm('Are you sure you want to remove this notification?')) {
    await notificationsState.deleteNotification(id)
  }
}
</script>
