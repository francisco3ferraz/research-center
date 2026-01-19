<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h2 class="text-2xl font-bold text-slate-800">Histórico de Atividade do Utilizador</h2>
          <p class="text-sm text-slate-500">Visualizar ações deste utilizador</p>
        </div>
        <NuxtLink to="/users" class="text-blue-600 hover:underline text-sm">Voltar a Utilizadores</NuxtLink>
      </div>

      <div v-if="loading" class="text-slate-600">A carregar histórico...</div>
      <div v-else>
        <div v-if="items.length === 0" class="text-center py-12 text-slate-500">
          <div class="text-4xl mb-3">📋</div>
          <div>Nenhuma atividade registada</div>
        </div>

        <div v-else class="space-y-3">
          <div v-for="log in items" :key="log.id" class="border-l-4 pl-4 py-3" :class="getActivityColor(log.actionType)">
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-2">
                  <span class="font-semibold text-slate-800">{{ getActionLabel(log.actionType) }}</span>
                  <span class="text-sm text-slate-500">{{ log.entityType }}</span>
                </div>
                <div class="text-sm text-slate-600 mt-1">{{ log.description }}</div>
                <div v-if="log.changedFields" class="text-xs text-slate-400 mt-1">
                  Campos alterados: {{ log.changedFields }}
                </div>
              </div>
              <div class="text-xs text-slate-400 whitespace-nowrap ml-4">
                {{ log.timestamp ? new Date(log.timestamp).toLocaleString() : '' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const api = useApi()
const route = useRoute()
const userId = route.params.id

const items = ref([])
const loading = ref(false)

const fetchActivity = async () => {
  loading.value = true
  try{
    const resp = await api.get(`/users/${userId}/activity`)
    // Endpoint returns List, not Page
    items.value = Array.isArray(resp.data) ? resp.data : []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const getActionLabel = (actionType) => {
  const labels = {
    'CREATE': '➕ Criado',
    'UPDATE': '✏️ Atualizado',
    'DELETE': '🗑️ Removido',
    'UPLOAD': '📤 Upload',
    'COMMENT': '💬 Comentário',
    'RATE': '⭐ Avaliação'
  }
  return labels[actionType] || actionType
}

const getActivityColor = (actionType) => {
  const colors = {
    'CREATE': 'border-green-400',
    'UPDATE': 'border-blue-400',
    'DELETE': 'border-red-400',
    'UPLOAD': 'border-purple-400',
    'COMMENT': 'border-yellow-400',
    'RATE': 'border-orange-400'
  }
  return colors[actionType] || 'border-gray-400'
}

onMounted(fetchActivity)
</script>
