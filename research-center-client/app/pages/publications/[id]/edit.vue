<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow-lg rounded-lg overflow-hidden">
      <div class="px-6 py-4 border-b">
        <div class="flex items-center justify-between gap-4">
          <div>
            <h1 class="text-2xl font-semibold text-slate-800">
              Edit Publication
            </h1>
            <div class="text-sm text-slate-500">
              Update the metadata of the publication
            </div>
          </div>
          <div class="flex items-center gap-3">
            <button
              @click="cancel"
              type="button"
              class="inline-flex items-center gap-2 px-3 py-2 border rounded text-slate-700 hover:bg-slate-50"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
              Cancel
            </button>
            <button
              @click.prevent="save"
              :disabled="saving || !form.title"
              class="inline-flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 disabled:opacity-50"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Save
            </button>
          </div>
        </div>
      </div>

      <div class="p-6">
        <div v-if="loading" class="text-slate-600">Loading data...</div>

        <form
          v-else
          @submit.prevent="save"
          class="grid grid-cols-1 lg:grid-cols-3 gap-6"
        >
          <div class="lg:col-span-2 space-y-4">
            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1">
                Title <span class="text-red-500">*</span>
              </div>
              <input
                v-model="form.title"
                placeholder="Title"
                class="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-200"
              />
            </label>

            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1">
                Abstract
              </div>
              <textarea
                v-model="form.abstract"
                placeholder="Short abstract"
                class="w-full border rounded px-3 py-2 focus:ring-2 focus:ring-blue-200"
                rows="8"
              ></textarea>
            </label>

            <label class="block">
              <div class="text-sm font-medium text-slate-600 mb-1 flex items-center justify-between">
                <div>
                  AI Generated Summary
                  <span class="text-xs text-slate-400"
                    >(optional - edit/correct if needed)</span
                  >
                </div>
                <button
                  @click="generateAISummary"
                  :disabled="isGeneratingSummary || !form.title || !form.abstract"
                  class="bg-blue-600 disabled:opacity-50 text-white px-3 py-1 rounded text-sm flex items-center gap-2 hover:bg-blue-700"
                  type="button"
                >
                  <svg
                    v-if="isGeneratingSummary"
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
                  <span>{{ isGeneratingSummary ? "Generating..." : "Generate AI Summary" }}</span>
                </button>
              </div>
              <textarea
                v-model="form.aiGeneratedSummary"
                placeholder="Automatically generated AI summary"
                class="w-full border rounded px-3 py-2 bg-blue-50 focus:ring-2 focus:ring-blue-200"
                :rows="form.aiGeneratedSummary && form.aiGeneratedSummary.length > 200 ? 8 : 5"
              ></textarea>
              <div v-if="aiError" class="text-red-600 text-sm mt-1">{{ aiError }}</div>
            </label>

            <div class="flex items-center gap-3 text-sm text-slate-500">
              <div>
                Last updated:
                <strong class="text-slate-700">{{
                  pub.updatedAt ? new Date(pub.updatedAt).toLocaleString() : "—"
                }}</strong>
              </div>
            </div>
          </div>

          <aside class="bg-slate-50 p-4 rounded lg:col-span-1">
            <div class="mb-3">
              <div class="text-sm font-medium text-slate-600 mb-1">Authors</div>
              <div class="flex flex-wrap gap-2 mb-2">
                <span
                  v-for="(a, i) in form.authors"
                  :key="i"
                  class="inline-flex items-center gap-2 bg-white border px-2 py-1 rounded-full text-sm"
                >
                  <span class="text-slate-800">{{ a }}</span>
                  <button
                    type="button"
                    @click="removeAuthor(i)"
                    class="text-slate-400 hover:text-red-600"
                  >
                    ×
                  </button>
                </span>
              </div>
              <input
                v-model="authorQuery"
                @input="lookupAuthors"
                @keydown.enter.prevent="addRawAuthor"
                placeholder="Search authors or add new..."
                class="w-full border rounded px-3 py-2"
              />
              <ul
                v-if="suggestions.length"
                class="mt-2 bg-white border rounded shadow max-h-40 overflow-auto"
              >
                <li
                  v-for="s in suggestions"
                  :key="s.id"
                  class="px-3 py-2 hover:bg-gray-50 cursor-pointer flex justify-between"
                  @click="addAuthorFromSuggestion(s)"
                >
                  <div>
                    <div class="font-medium text-slate-800">{{ s.name }}</div>
                    <div class="text-xs text-slate-500">
                      {{ s.username || s.email }}
                    </div>
                  </div>
                  <div class="text-sm text-slate-400">+</div>
                </li>
              </ul>
            </div>

            <div class="grid grid-cols-1 gap-3">
              <label>
                <div class="text-sm text-slate-600 mb-1">Year</div>
                <input
                  v-model.number="form.year"
                  type="number"
                  class="w-full border rounded px-3 py-2"
                />
              </label>

              <label>
                <div class="text-sm text-slate-600 mb-1">Publisher</div>
                <input
                  v-model="form.publisher"
                  class="w-full border rounded px-3 py-2"
                />
              </label>

              <label>
                <div class="text-sm text-slate-600 mb-1">DOI</div>
                <input
                  v-model="form.doi"
                  class="w-full border rounded px-3 py-2"
                />
              </label>

              <label>
                 <div class="text-sm text-slate-600 mb-1">
                    Update PDF File
                    <span v-if="pub.documentId" class="text-xs text-green-600 ml-1">(File already exists)</span>
                 </div>
                 <input
                   type="file"
                   accept=".pdf"
                   @change="handleFileChange"
                   class="w-full border rounded px-3 py-2 text-sm"
                 />
              </label>

              <!-- Visibility and Confidentiality Checkboxes -->
              <div class="bg-white border rounded p-3 space-y-3 mt-4">
                <label class="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    v-model="form.visible"
                    class="form-checkbox h-4 w-4 text-blue-600 rounded"
                  />
                  <span class="text-sm font-medium text-slate-800"
                    >Visible</span
                  >
                </label>

                <label class="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    v-model="form.confidential"
                    class="form-checkbox h-4 w-4 text-red-600 rounded"
                  />
                  <span class="text-sm font-medium text-slate-800"
                    >Confidential</span
                  >
                </label>
              </div>

              <!-- Tags Management (EP37/EP38) -->
              <div class="bg-white border rounded p-3 mt-4">
                <div class="text-sm font-medium text-slate-600 mb-2">Tags</div>
                <div class="flex flex-wrap gap-2 mb-2">
                  <span
                    v-for="t in publicationTags"
                    :key="t.id"
                    class="inline-flex items-center gap-1 bg-gray-100 px-2 py-1 rounded-full text-sm"
                  >
                    <span class="text-slate-800">{{ t.name }}</span>
                    <button
                      type="button"
                      @click="removeTagFromPublication(t.id)"
                      class="text-slate-400 hover:text-red-600"
                    >
                      ×
                    </button>
                  </span>
                  <span v-if="publicationTags.length === 0" class="text-sm text-slate-400">No tags</span>
                </div>
                <select
                  v-model="selectedTagId"
                  @change="addTagToPublication"
                  class="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="">Add a tag...</option>
                  <option
                    v-for="t in availableTags"
                    :key="t.id"
                    :value="t.id"
                  >
                    {{ t.name }}
                  </option>
                </select>
              </div>
            </div>

            <div class="mt-4 text-xs text-slate-500">
              Fields with <span class="text-red-500">*</span> are required.
            </div>
          </aside>
        </form>

        <div v-if="error" class="text-red-600 mt-3">{{ error }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
const route = useRoute();
const id = route.params.id;
const api = useApi();
const auth = useAuth();

const pub = ref({});

const loading = ref(true);
const saving = ref(false);
const error = ref(null);
const isGeneratingSummary = ref(false);
const aiError = ref(null);
const form = ref({
  title: "",
  authors: [],
  abstract: "",
  aiGeneratedSummary: "",
  year: null,
  publisher: "",
  doi: "",
  visible: true,
  confidential: false,
});

const authorQuery = ref("");
const suggestions = ref([]);

// Tags management state (EP37/EP38)
const allTags = ref([]);
const publicationTags = ref([]);
const selectedTagId = ref("");

const availableTags = computed(() => {
  const existingIds = new Set(publicationTags.value.map(t => t.id));
  return allTags.value.filter(t => !existingIds.has(t.id));
});


const load = async () => {
  loading.value = true;
  try {
    const resp = await api.get(`/publications/${id}`);
    const p = resp.data;
    
    const currentUser = auth.user.value;
    if (!currentUser) return navigateTo('/auth/login');
    
    if (currentUser.role !== 'ADMINISTRADOR') {
        const ownerId = p.uploadedBy ? p.uploadedBy.id : null;
        if (!ownerId || ownerId !== currentUser.id) {
             console.warn("Unauthorized edit attempt - not the owner");
             return navigateTo(`/publications/${id}`);
        }
    }

    pub.value = p || {};
    form.value.title = p.title || "";
    form.value.authors = Array.isArray(p.authors) ? p.authors.slice() : [];
    form.value.abstract = p.abstract || p.abstract_ || "";
    form.value.aiGeneratedSummary = p.aiGeneratedSummary || "";
    form.value.year = p.year || null;
    form.value.publisher = p.publisher || "";
    form.value.doi = p.doi || "";
    form.value.visible = p.visible !== undefined ? p.visible : true;
    form.value.confidential = p.confidential || false;
    
    // Load publication tags
    publicationTags.value = Array.isArray(p.tags) ? p.tags.slice() : [];
    
    // Load all available tags
    const tagsResp = await api.get('/tags');
    allTags.value = tagsResp.data || [];
  } catch (e) {
    console.error(e);
    error.value = e?.response?.data?.message || "Error loading publication";
  } finally {
    loading.value = false;
  }
};

const lookupAuthors = async () => {
  suggestions.value = [];
  const q = authorQuery.value && authorQuery.value.trim();
  if (!q) return;
  try {
    const resp = await api.get(`/users/lookup?q=${encodeURIComponent(q)}`);
    suggestions.value = resp.data || [];
  } catch (e) {
    console.error(e);
  }
};

const addAuthorFromSuggestion = (s) => {
  if (!form.value.authors.includes(s.name)) form.value.authors.push(s.name);
  authorQuery.value = "";
  suggestions.value = [];
};

const removeAuthor = (i) => {
  form.value.authors.splice(i, 1);
};

const addRawAuthor = () => {
  const v = authorQuery.value && authorQuery.value.trim();
  if (!v) return;
  if (!form.value.authors.includes(v)) form.value.authors.push(v);
  authorQuery.value = "";
  suggestions.value = [];
};

const generateAISummary = async () => {
  aiError.value = null;
  
  // Validate that we have title and abstract
  if (!form.value.title || !form.value.title.trim()) {
    aiError.value = "Please fill in the title first";
    return;
  }
  
  if (!form.value.abstract || !form.value.abstract.trim()) {
    aiError.value = "Please fill in the abstract first";
    return;
  }
  
  isGeneratingSummary.value = true;
  
  try {
    const response = await api.post("/ai/generate-summary", {
      title: form.value.title,
      abstract: form.value.abstract
    });
    
    if (response.data && response.data.summary) {
      form.value.aiGeneratedSummary = response.data.summary;
    } else {
      aiError.value = "Invalid server response";
    }
  } catch (e) {
    console.error("Error generating AI summary:", e);
    if (e?.response?.data?.message) {
      aiError.value = e.response.data.message;
    } else if (e?.response?.status === 503) {
      aiError.value = "AI service unavailable. Make sure Ollama is running.";
    } else {
      aiError.value = "Error generating summary. Please try again.";
    }
  } finally {
    isGeneratingSummary.value = false;
  }
};

const file = ref(null);

const handleFileChange = (e) => {
  if (e.target.files && e.target.files.length > 0) {
    file.value = e.target.files[0];
  } else {
    file.value = null;
  }
};

const save = async () => {
  saving.value = true;
  error.value = null;
  try {
    const payload = {
      title: form.value.title,
      authors: form.value.authors,
      abstract: form.value.abstract,
      aiGeneratedSummary: form.value.aiGeneratedSummary || null,
      year: form.value.year,
      publisher: form.value.publisher,
      doi: form.value.doi,
      confidential: form.value.confidential,
      visible: form.value.visible
    };
    await api.put(`/publications/${id}`, payload);
    
    // Upload file if selected
    if (file.value) {
       const formData = new FormData();
       formData.append('file', file.value);
       await api.post(`/publications/${id}/file`, formData);
    }

    navigateTo(`/publications/${id}`);
  } catch (e) {
    error.value = e?.response?.data?.message || "Error saving publication";
  } finally {
    saving.value = false;
  }
};

const cancel = () => navigateTo(`/publications/${id}`);

// EP37: Add tag to publication
const addTagToPublication = async () => {
  if (!selectedTagId.value) return;
  const tagId = parseInt(selectedTagId.value);
  try {
    await api.post(`/publications/${id}/tags`, { tagId });
    const tag = allTags.value.find(t => t.id === tagId);
    if (tag) {
      publicationTags.value.push(tag);
    }
    selectedTagId.value = "";
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error adding tag");
  }
};

// EP38: Remove tag from publication
const removeTagFromPublication = async (tagId) => {
  try {
    await api.delete(`/publications/${id}/tags/${tagId}`);
    publicationTags.value = publicationTags.value.filter(t => t.id !== tagId);
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error removing tag");
  }
};

load();
</script>

<style scoped>
.suggestion {
  cursor: pointer;
}
</style>
