export const useNotifications = () => {
  const notifications = useState('notifications', () => [])
  const unreadCount = useState('unreadCount', () => 0)
  const loading = useState('notificationsLoading', () => false)
  const api = useApi()

  const fetchNotifications = async () => {
    loading.value = true
    try {
      const response = await api.get('/notifications')
      // API returns { content: [], totalElements: number, unreadCount: number }
      notifications.value = response.data?.content || []
      unreadCount.value = response.data?.unreadCount || 0
    } catch (e) {
      console.error('Failed to fetch notifications', e)
      notifications.value = []
      unreadCount.value = 0
    } finally {
      loading.value = false
    }
  }

  const updateUnreadCount = () => {
    const notifArray = Array.isArray(notifications.value) ? notifications.value : []
    unreadCount.value = notifArray.filter(n => !n.read).length
  }

  const markAsRead = async (id) => {
    try {
      const response = await api.patch(`/notifications/${id}/read`)
      const notifArray = Array.isArray(notifications.value) ? notifications.value : []
      const notification = notifArray.find(n => n.id === id)
      if (notification) {
        notification.read = true
        notification.readAt = response.data?.readAt || new Date().toISOString()
        updateUnreadCount()
      }
    } catch (e) {
      console.error('Failed to mark as read', e)
    }
  }

  const markAllAsRead = async () => {
    try {
      await api.patch('/notifications/read-all')
      // Update local state - mark all as read
      const notifArray = Array.isArray(notifications.value) ? notifications.value : []
      notifArray.forEach(n => {
        n.read = true
        n.readAt = new Date().toISOString()
      })
      unreadCount.value = 0
    } catch (e) {
      console.error('Failed to mark all as read', e)
    }
  }

  const deleteNotification = async (id) => {
    try {
      await api.delete(`/notifications/${id}`)
      const notifArray = Array.isArray(notifications.value) ? notifications.value : []
      notifications.value = notifArray.filter(n => n.id !== id)
      updateUnreadCount()
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

  return {
    notifications,
    unreadCount,
    loading,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    getNotificationIcon,
    formatDate
  }
}
