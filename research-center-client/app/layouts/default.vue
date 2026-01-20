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
                My Publications
              </NuxtLink>

              <NuxtLink
                v-if="auth.token.value"
                to="/subscriptions"
                class="border-transparent text-gray-500 hover:border-blue-500 hover:text-gray-700 inline-flex items-center px-1 pt-1 border-b-2 text-sm font-medium"
              >
                Subscriptions
              </NuxtLink>

            </div>
          </div>
          <div class="flex items-center">
            <template v-if="auth.token.value">
              <!-- Notifications Dropdown -->
              <div class="relative mr-4">
                <button
                  @click="toggleNotifications"
                  class="relative p-2 text-gray-500 hover:text-gray-700 focus:outline-none"
                  title="Notifications"
                >
                  <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                  </svg>
                  <span
                    v-if="notificationsState.unreadCount.value > 0"
                    class="absolute top-0 right-0 inline-flex items-center justify-center px-1.5 py-0.5 text-xs font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-red-600 rounded-full min-w-[1.25rem]"
                  >
                    {{ notificationsState.unreadCount.value > 99 ? '99+' : notificationsState.unreadCount.value }}
                  </span>
                </button>

                <!-- Dropdown Panel -->
                <div
                  v-if="notificationsOpen"
                  v-click-outside="closeNotifications"
                  class="absolute right-0 mt-2 w-96 bg-white shadow-xl rounded-lg ring-1 ring-black ring-opacity-5 z-50 origin-top-right max-h-[32rem] overflow-hidden flex flex-col"
                >
                  <!-- Header -->
                  <div class="px-4 py-3 border-b border-gray-200 flex justify-between items-center bg-gray-50">
                    <h3 class="text-sm font-semibold text-gray-900">Notifications</h3>
                    <div class="flex gap-2">
                      <button
                        v-if="notificationsState.unreadCount.value > 0"
                        @click="handleMarkAllAsRead"
                        class="text-xs text-blue-600 hover:text-blue-500"
                      >
                        Mark all as read
                      </button>
                      <NuxtLink
                        to="/notifications"
                        @click="notificationsOpen = false"
                        class="text-xs text-blue-600 hover:text-blue-500"
                      >
                        View all
                      </NuxtLink>
                    </div>
                  </div>

                  <!-- Notifications List -->
                  <div class="overflow-y-auto flex-1">
                    <div v-if="notificationsState.loading.value" class="p-4 text-center text-sm text-gray-500">
                      Loading...
                    </div>
                    <div v-else-if="notificationsState.notifications.value.length === 0" class="p-8 text-center">
                      <div class="text-gray-400 text-3xl mb-2">🔔</div>
                      <p class="text-sm text-gray-500">No notifications</p>
                    </div>
                    <div v-else class="divide-y divide-gray-100">
                      <div
                        v-for="notification in recentNotifications"
                        :key="notification.id"
                        class="p-3 hover:bg-gray-50 transition-colors"
                        :class="{ 'bg-blue-50': !notification.read }"
                      >
                        <div class="flex items-start gap-3">
                          <span class="text-lg flex-shrink-0">{{ notificationsState.getNotificationIcon(notification.type) }}</span>
                          <div class="flex-1 min-w-0">
                            <div class="flex items-start justify-between gap-2">
                              <h4 class="text-sm font-medium text-gray-900 truncate">{{ notification.title }}</h4>
                              <span v-if="!notification.read" class="flex-shrink-0 w-2 h-2 bg-blue-600 rounded-full mt-1"></span>
                            </div>
                            <p class="text-xs text-gray-600 mt-0.5 line-clamp-2">{{ notification.message }}</p>
                            <div class="flex items-center justify-between mt-2">
                              <span class="text-xs text-gray-400">{{ notificationsState.formatDate(notification.createdAt) }}</span>
                              <div class="flex gap-2">
                                <button
                                  v-if="!notification.read"
                                  @click="handleMarkAsRead(notification.id)"
                                  class="text-xs text-blue-600 hover:text-blue-500"
                                  title="Mark as read"
                                >
                                  ✓
                                </button>
                                <button
                                  @click="handleDeleteNotification(notification.id)"
                                  class="text-xs text-red-600 hover:text-red-500"
                                  title="Remove"
                                >
                                  🗑️
                                </button>
                              </div>
                            </div>
                            <!-- Link to related content -->
                            <div v-if="notification.relatedEntityType && notification.relatedEntityId" class="mt-2">
                              <NuxtLink
                                v-if="notification.relatedEntityType === 'PUBLICATION'"
                                :to="`/publications/${notification.relatedEntityId}`"
                                @click="notificationsOpen = false"
                                class="text-xs text-blue-600 hover:underline"
                              >
                                View publication →
                              </NuxtLink>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

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
                    >Manage Users</NuxtLink>
                    <NuxtLink
                      to="/tags"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Manage Tags</NuxtLink>
                    <NuxtLink
                      v-if="auth.user.value?.role === 'ADMINISTRADOR'"
                      to="/scientific-areas"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Manage Scientific Areas</NuxtLink>
                    <NuxtLink
                      to="/admin/hidden"
                      class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >Hidden Content</NuxtLink>
                  </div>
                </div>
              </div>

              <NuxtLink
                to="/profile"
                class="text-sm text-gray-500 mr-4 hover:text-gray-700"
                >Profile</NuxtLink
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
import { ref, computed, onMounted, watch } from "vue";

const auth = useAuth();
const notificationsState = useNotifications();

// Admin dropdown state
const adminOpen = ref(false);

// Notifications dropdown state
const notificationsOpen = ref(false);

// Fetch notifications when user is authenticated
onMounted(() => {
  auth.initAuth();
});

watch(() => auth.token.value, (newToken) => {
  if (newToken) {
    notificationsState.fetchNotifications();
    // Poll for new notifications every 30 seconds
    const interval = setInterval(() => {
      if (auth.token.value) {
        notificationsState.fetchNotifications();
      } else {
        clearInterval(interval);
      }
    }, 30000);
  }
}, { immediate: true });

const handleLogout = () => {
  auth.logout();
};

const toggleNotifications = () => {
  notificationsOpen.value = !notificationsOpen.value;
  if (notificationsOpen.value && adminOpen.value) {
    adminOpen.value = false;
  }
};

const closeNotifications = () => {
  notificationsOpen.value = false;
};

// Show only the 5 most recent notifications in dropdown
const recentNotifications = computed(() => {
  const notifArray = Array.isArray(notificationsState.notifications.value) 
    ? notificationsState.notifications.value 
    : [];
  return notifArray.slice(0, 5);
});

const handleMarkAsRead = async (id) => {
  await notificationsState.markAsRead(id);
};

const handleMarkAllAsRead = async () => {
  await notificationsState.markAllAsRead();
};

const handleDeleteNotification = async (id) => {
  await notificationsState.deleteNotification(id);
};

// Click outside directive
const vClickOutside = {
  mounted(el, binding) {
    // Delay adding the event listener to avoid catching the click that opened the dropdown
    setTimeout(() => {
      el.clickOutsideEvent = (event) => {
        if (!(el === event.target || el.contains(event.target))) {
          binding.value();
        }
      };
      document.addEventListener('click', el.clickOutsideEvent);
    }, 0);
  },
  unmounted(el) {
    if (el.clickOutsideEvent) {
      document.removeEventListener('click', el.clickOutsideEvent);
    }
  }
};
</script>
