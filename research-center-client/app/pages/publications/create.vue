<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6 max-w-3xl mx-auto">
      <h2 class="text-2xl font-bold mb-4">Submeter Nova Publicação</h2>
      <div class="space-y-4">
        <label class="block">
          <div class="text-sm text-slate-600 mb-1">Título</div>
          <input
            v-model="title"
            placeholder="Título"
            class="w-full border rounded px-3 py-2"
          />
          <div v-if="validationErrors.title" class="text-red-600 text-sm mt-1">
            {{ validationErrors.title }}
          </div>
        </label>

        <label class="block relative">
          <div class="text-sm text-slate-600 mb-1">Autores</div>
          <div class="flex gap-2 items-center flex-wrap">
            <template v-for="(a, i) in selectedAuthors" :key="i">
              <span
                class="inline-flex items-center bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-sm"
              >
                <span class="mr-2">{{ a }}</span>
                <button
                  @click.prevent="removeAuthor(i)"
                  class="text-blue-600 hover:text-blue-900 text-xs font-bold"
                >
                  ×
                </button>
              </span>
            </template>
          </div>
          <input
            v-model="authorQuery"
            @input="onAuthorInput"
            @keydown.enter.prevent="addAuthorFromQuery"
            placeholder="Procurar autores..."
            class="w-full border rounded px-3 py-2 mt-2"
          />
          <ul
            v-if="showSuggestions"
            class="absolute z-20 bg-white border rounded mt-1 w-full max-h-48 overflow-auto"
          >
            <li
              v-for="u in suggestions"
              :key="u.id"
              @click="selectSuggestion(u)"
              class="px-3 py-2 hover:bg-gray-100 cursor-pointer"
            >
              {{ u.name }}
            </li>
            <li
              v-if="suggestions.length === 0"
              class="px-3 py-2 text-sm text-slate-500"
            >
              Nenhum utilizador encontrado
            </li>
          </ul>
        </label>

        <div class="flex gap-4 items-center">
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">Ano</div>
            <input
              v-model="year"
              type="number"
              class="w-32 border rounded px-3 py-2"
            />
          </label>
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">Ficheiro</div>
            <div class="flex items-center gap-3">
              <input type="file" @change="onFileChange" class="w-full" />
              <div v-if="file && file.name" class="text-sm text-slate-600">
                {{ file.name }}
              </div>
            </div>
          </label>
        </div>

        <div class="flex gap-4 items-center">
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">Tipo</div>
            <select v-model="type" class="w-full border rounded px-3 py-2">
              <option value="">-- Selecionar --</option>
              <option
                v-for="t in publicationTypes"
                :key="t.code"
                :value="t.code"
              >
                {{ t.name }}
              </option>
            </select>
            <div v-if="validationErrors.type" class="text-red-600 text-sm mt-1">
              {{ validationErrors.type }}
            </div>
          </label>
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">Área Científica</div>
            <select
              v-model="areaScientific"
              class="w-full border rounded px-3 py-2"
            >
              <option value="">-- Selecionar --</option>
              <option v-for="a in scientificAreas" :key="a.id" :value="a.name">
                {{ a.name }}
              </option>
            </select>
            <div
              v-if="scientificAreas.length === 0"
              class="text-yellow-600 text-sm mt-1"
            >
              Nenhuma área científica disponível — peça a um administrador para
              criar.
            </div>
            <div
              v-if="validationErrors.areaScientific"
              class="text-red-600 text-sm mt-1"
            >
              {{ validationErrors.areaScientific }}
            </div>
          </label>
        </div>

        <label class="block">
          <div class="text-sm text-slate-600 mb-1">Resumo</div>
          <textarea
            v-model="abstract"
            placeholder="Resumo"
            class="w-full border rounded px-3 py-2"
            rows="6"
          ></textarea>
        </label>

        <label class="block">
          <div class="text-sm text-slate-600 mb-1">
            Resumo Gerado por IA
            <span class="text-xs text-slate-400"
              >(opcional - será gerado automaticamente ou pode inserir
              manualmente)</span
            >
          </div>
          <textarea
            v-model="aiGeneratedSummary"
            placeholder="Resumo gerado automaticamente por IA (pode editar/corrigir se necessário)"
            class="w-full border rounded px-3 py-2 bg-blue-50"
            rows="4"
          ></textarea>
        </label>

        <label class="block">
          <div class="text-sm text-slate-600 mb-1">
            Tags
            <span class="text-xs text-slate-400"
              >(opcional - selecione uma ou mais)</span
            >
          </div>
          <div class="flex flex-wrap gap-2 mb-2">
            <span
              v-for="tag in selectedTags"
              :key="tag.id"
              class="inline-flex items-center gap-2 bg-blue-100 text-blue-800 px-3 py-1 rounded-full text-sm"
            >
              {{ tag.name }}
              <button
                type="button"
                @click="removeTag(tag.id)"
                class="text-blue-600 hover:text-blue-900 font-bold"
              >
                ×
              </button>
            </span>
          </div>
          <select
            v-model="tagToAdd"
            @change="addTag"
            class="w-full border rounded px-3 py-2"
          >
            <option value="">-- Selecionar tag --</option>
            <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">
              {{ tag.name }}
            </option>
          </select>
          <div
            v-if="availableTags.length === 0"
            class="text-yellow-600 text-sm mt-1"
          >
            Nenhuma tag disponível — peça a um responsável para criar.
          </div>
        </label>

        <div
          class="flex flex-col sm:flex-row gap-6 border p-4 rounded bg-slate-50"
        >
          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              v-model="visible"
              class="form-checkbox h-5 w-5 text-blue-600 rounded"
            />
            <div>
              <div class="font-medium text-slate-800">Publicação Visível</div>
              <div class="text-xs text-slate-500">
                Se desmarcado, a publicação ficará oculta para o público
              </div>
            </div>
          </label>

          <label class="flex items-center gap-2 cursor-pointer">
            <input
              type="checkbox"
              v-model="confidential"
              class="form-checkbox h-5 w-5 text-red-600 rounded"
            />
            <div>
              <div class="font-medium text-slate-800">Confidencial</div>
              <div class="text-xs text-slate-500">
                Apenas administradores e responsáveis poderão ver
              </div>
            </div>
          </label>
        </div>

        <div class="flex items-center gap-3">
          <button
            @click="submit"
            :disabled="isSubmitting"
            class="bg-green-600 disabled:opacity-50 text-white px-4 py-2 rounded shadow flex items-center gap-2"
          >
            <svg
              v-if="isSubmitting"
              class="animate-spin h-4 w-4 text-white"
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
            >
              <circle
                class="opacity-25"
                cx="12"
                cy="12"
                r="10"
                stroke="currentColor"
                stroke-width="4"
              ></circle>
              <path
                class="opacity-75"
                fill="currentColor"
                d="M4 12a8 8 0 018-8v8H4z"
              ></path>
            </svg>
            <span>{{ isSubmitting ? "A submeter..." : "Submeter" }}</span>
          </button>
          <NuxtLink to="/" class="px-4 py-2 border rounded">Cancelar</NuxtLink>
        </div>

        <div v-if="error" class="text-red-600">{{ error }}</div>
        <div v-if="fileError" class="text-red-600">{{ fileError }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
const api = useApi();
const auth = useAuth();

const title = ref("");
const authors = ref("");
const authorQuery = ref("");
const suggestions = ref([]);
const selectedAuthors = ref([]);
const showSuggestions = ref(false);
let authorDebounce = null;
const year = ref(new Date().getFullYear());
const abstract = ref("");
const aiGeneratedSummary = ref("");
const publicationTypes = ref([]);
const scientificAreas = ref([]);
const type = ref("");
const areaScientific = ref("");
const visible = ref(true); // Default to visible
const confidential = ref(false); // Default to not confidential
const file = ref(null);
const error = ref(null);
const fileError = ref(null);
const validationErrors = ref({});
const isSubmitting = ref(false);

// Tag management
const availableTags = ref([]);
const selectedTags = ref([]);
const tagToAdd = ref("");

const onFileChange = (e) => {
  file.value = e.target.files[0];
  fileError.value = null;

  // Validate file type (PDF or ZIP only as per spec)
  if (file.value) {
    const validTypes = [
      "application/pdf",
      "application/zip",
      "application/x-zip-compressed",
    ];
    const validExtensions = [".pdf", ".zip"];
    const fileName = file.value.name.toLowerCase();
    const fileType = file.value.type;

    const hasValidExtension = validExtensions.some((ext) =>
      fileName.endsWith(ext),
    );
    const hasValidType = validTypes.includes(fileType);

    if (!hasValidExtension && !hasValidType) {
      fileError.value = "Apenas ficheiros PDF ou ZIP são permitidos";
      file.value = null;
    }
  }
};

const fetchUsers = async (q) => {
  try {
    // Use the public lookup endpoint which returns minimal user info (id + name)
    const resp = await api.get("/users/lookup", { params: { q } });
    suggestions.value = resp.data || [];
    showSuggestions.value = true;
  } catch (e) {
    console.error("Failed to fetch users", e);
    suggestions.value = [];
    showSuggestions.value = false;
  }
};

const fetchAuxData = async () => {
  try {
    const [typesResp, areasResp, tagsResp] = await Promise.all([
      api.get("/publication-types"),
      api.get("/scientific-areas"),
      api.get("/tags"),
    ]);
    publicationTypes.value = typesResp.data || [];
    scientificAreas.value = areasResp.data || [];
    availableTags.value = (tagsResp.data || []).filter((t) => t.visible);
  } catch (e) {
    console.warn("Failed to fetch auxiliary data", e);
  }
};

const addTag = () => {
  if (!tagToAdd.value) return;
  const tag = availableTags.value.find(
    (t) => t.id === parseInt(tagToAdd.value),
  );
  if (tag && !selectedTags.value.find((t) => t.id === tag.id)) {
    selectedTags.value.push(tag);
  }
  tagToAdd.value = "";
};

const removeTag = (tagId) => {
  selectedTags.value = selectedTags.value.filter((t) => t.id !== tagId);
};

const onAuthorInput = (e) => {
  const v = authorQuery.value;
  if (authorDebounce) clearTimeout(authorDebounce);
  if (!v || v.trim().length < 1) {
    suggestions.value = [];
    showSuggestions.value = false;
    return;
  }
  authorDebounce = setTimeout(() => fetchUsers(v.trim()), 300);
};

const selectSuggestion = (u) => {
  const name = u.name || u.username;
  if (!selectedAuthors.value.includes(name)) selectedAuthors.value.push(name);
  authorQuery.value = "";
  suggestions.value = [];
  showSuggestions.value = false;
};

const addAuthorFromQuery = () => {
  const v = authorQuery.value && authorQuery.value.trim();
  if (v && !selectedAuthors.value.includes(v)) selectedAuthors.value.push(v);
  authorQuery.value = "";
  suggestions.value = [];
  showSuggestions.value = false;
};

const removeAuthor = (idx) => {
  selectedAuthors.value.splice(idx, 1);
};

const submit = async () => {
  error.value = null;
  validationErrors.value = {};
  if (!auth.token.value) return navigateTo("/auth/login");

  // Client-side validation
  if (!title.value || !title.value.trim())
    validationErrors.value.title = "Título é obrigatório";
  if (!type.value) validationErrors.value.type = "Tipo é obrigatório";
  if (!areaScientific.value)
    validationErrors.value.areaScientific = "Área científica é obrigatória";
  if (!year.value) validationErrors.value.year = "Ano é obrigatório";
  if (scientificAreas.value.length === 0) {
    error.value =
      "Não existem áreas científicas definidas no sistema. Contacte um administrador.";
    return;
  }
  if (Object.keys(validationErrors.value).length > 0) {
    error.value = "Campos obrigatórios em falta";
    return;
  }

  isSubmitting.value = true;
  try {
    // Ensure authors is sent as an array of strings and use 'abstract' key
    const authorsArray =
      selectedAuthors.value.length > 0
        ? selectedAuthors.value.slice()
        : typeof authors.value === "string"
          ? authors.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean)
          : Array.isArray(authors.value)
            ? authors.value
            : [];

    const metadata = {
      title: title.value,
      authors: authorsArray,
      type: type.value || null,
      areaScientific: areaScientific.value || null,
      year: year.value,
      abstract: abstract.value,
      aiGeneratedSummary: aiGeneratedSummary.value || null,
      tags: selectedTags.value.map((t) => ({ id: t.id })),
      visible: visible.value,
      confidential: confidential.value,
    };
    const form = new FormData();
    form.append("metadata", JSON.stringify(metadata));
    if (file.value) form.append("file", file.value, file.value.name);

    // Let the browser set Content-Type (with boundary)
    const resp = await api.post("/publications", form);
    const created = resp.data;
    navigateTo(`/publications/${created.id}`);
  } catch (e) {
    console.error(e);
    // Show full server response when available to help debugging
    if (e?.response) {
      try {
        const d = e.response.data;
        error.value =
          typeof d === "string" ? d : d?.message || JSON.stringify(d);
      } catch (ex) {
        error.value = "Erro ao submeter (400). Veja o console para detalhes.";
      }
    } else {
      error.value = "Erro ao submeter (sem resposta do servidor)";
    }
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  fetchAuxData();
});
</script>
