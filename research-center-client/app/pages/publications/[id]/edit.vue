<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow-lg rounded-lg overflow-hidden">
      <div class="px-6 py-4 border-b">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl font-semibold text-slate-800">Editar Publicação</h1>
            <div class="text-sm text-slate-500">Atualize os metadados da publicação</div>
          </div>
          <div class="flex items-center gap-3">
            <button @click="cancel" type="button" class="inline-flex items-center gap-2 px-3 py-2 border rounded text-slate-700 hover:bg-slate-50">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
              Cancelar
            </button>
            <button @click.prevent="save" :disabled="saving || !form.title" class="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 disabled:opacity-50">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/></svg>
              Guardar
            </button>
          </div>
        </div>
      </div>

      <div class="p-6">
        <div v-if="loading" class="text-slate-600">A carregar dados...</div>

        <form v-else @submit.prevent="save" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div class="lg:col-span-2 space-y-4">
            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1">Título <span class="text-red-500">*</span></div>
              <input v-model="form.title" placeholder="Título da publicação" class="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-200" />
            </label>

            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1">Resumo / Abstract</div>
              <textarea v-model="form.abstract" placeholder="Resumo curto" class="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-200" rows="8"></textarea>
            </label>

            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1">Resumo Gerado por IA <span class="text-xs text-slate-400">(opcional - pode editar/corrigir)</span></div>
              <textarea v-model="form.aiGeneratedSummary" placeholder="Resumo gerado automaticamente por IA" class="w-full border rounded px-3 py-2 bg-blue-50 focus:ring-2 focus:ring-blue-200" rows="5"></textarea>
            </label>

            <div class="flex items-center gap-3 text-sm text-slate-500">
              <div>Última atualização: <strong class="text-slate-700">{{ pub.updatedAt ? new Date(pub.updatedAt).toLocaleString() : '—' }}</strong></div>
            </div>
          </div>

          <aside class="bg-slate-50 p-4 rounded lg:col-span-1">
            <div class="mb-3">
              <div class="text-sm font-medium text-slate-600 mb-1">Autores</div>
              <div class="flex flex-wrap gap-2 mb-2">
                <span v-for="(a, i) in form.authors" :key="i" class="inline-flex items-center gap-2 bg-white border px-2 py-1 rounded-full text-sm">
                  <span class="text-slate-800">{{ a }}</span>
                  <button type="button" @click="removeAuthor(i)" class="text-slate-400 hover:text-red-600">×</button>
                </span>
              </div>
              <input v-model="authorQuery" @input="lookupAuthors" @keydown.enter.prevent="addRawAuthor" placeholder="Procurar autores ou adicionar novo..." class="w-full border rounded px-3 py-2" />
              <ul v-if="suggestions.length" class="mt-2 bg-white border rounded shadow max-h-40 overflow-auto">
                <li v-for="s in suggestions" :key="s.id" class="px-3 py-2 hover:bg-gray-50 cursor-pointer flex justify-between" @click="addAuthorFromSuggestion(s)">
                  <div>
                    <div class="font-medium text-slate-800">{{ s.name }}</div>
                    <div class="text-xs text-slate-500">{{ s.username || s.email }}</div>
                  </div>
                  <div class="text-sm text-slate-400">+</div>
                </li>
              </ul>
            </div>

            <div class="grid grid-cols-1 gap-3">
              <label>
                <div class="text-sm text-slate-600 mb-1">Ano</div>
                <input v-model.number="form.year" type="number" class="w-full border rounded px-3 py-2" />
              </label>

              <label>
                <div class="text-sm text-slate-600 mb-1">Editora</div>
                <input v-model="form.publisher" class="w-full border rounded px-3 py-2" />
              </label>

              <label>
                <div class="text-sm text-slate-600 mb-1">DOI</div>
                <input v-model="form.doi" class="w-full border rounded px-3 py-2" />
              </label>
            </div>

            <div class="mt-4 text-xs text-slate-500">Campos com <span class="text-red-500">*</span> são obrigatórios.</div>
          </aside>
        </form>

        <div v-if="error" class="text-red-600 mt-3">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const route = useRoute()
const id = route.params.id
const api = useApi()
const auth = useAuth()

const pub = ref({})

const loading = ref(true)
const saving = ref(false)
const error = ref(null)
const form = ref({ title: '', authors: [], abstract: '', aiGeneratedSummary: '', year: null, publisher: '', doi: '' })

const authorQuery = ref('')
const suggestions = ref([])

const load = async () => {
  loading.value = true
  try {
    const resp = await api.get(`/publications/${id}`)
    const p = resp.data
    pub.value = p || {}
    form.value.title = p.title || ''
    form.value.authors = Array.isArray(p.authors) ? p.authors.slice() : []
    form.value.abstract = p.abstract || p.abstract_ || ''
    form.value.aiGeneratedSummary = p.aiGeneratedSummary || ''
    form.value.year = p.year || null
    form.value.publisher = p.publisher || ''
    form.value.doi = p.doi || ''
  } catch (e) { console.error(e); error.value = e?.response?.data?.message || 'Erro ao carregar publicação' }
  finally { loading.value = false }
}

const lookupAuthors = async () => {
  suggestions.value = []
  const q = authorQuery.value && authorQuery.value.trim()
  if (!q) return
  try {
    const resp = await api.get(`/users/lookup?q=${encodeURIComponent(q)}`)
    suggestions.value = resp.data || []
  } catch (e) { console.error(e) }
}

const addAuthorFromSuggestion = (s) => {
  if (!form.value.authors.includes(s.name)) form.value.authors.push(s.name)
  authorQuery.value = ''
  suggestions.value = []
}

const removeAuthor = (i) => { form.value.authors.splice(i, 1) }

const addRawAuthor = () => {
  const v = authorQuery.value && authorQuery.value.trim()
  if (!v) return
  if (!form.value.authors.includes(v)) form.value.authors.push(v)
  authorQuery.value = ''
  suggestions.value = []
}

const save = async () => {
  saving.value = true
  error.value = null
  try {
    const payload = {
      title: form.value.title,
      authors: form.value.authors,
      abstract_: form.value.abstract,
      aiGeneratedSummary: form.value.aiGeneratedSummary || null,
      year: form.value.year,
      publisher: form.value.publisher,
      doi: form.value.doi
    }
    await api.put(`/publications/${id}`, payload)
    navigateTo(`/publications/${id}`)
  } catch (e) {
    console.error(e)
    error.value = e?.response?.data?.message || 'Erro ao guardar publicação'
  } finally { saving.value = false }
}

const cancel = () => navigateTo(`/publications/${id}`)

load()
</script>

<style scoped>
.suggestion { cursor: pointer }
</style>
