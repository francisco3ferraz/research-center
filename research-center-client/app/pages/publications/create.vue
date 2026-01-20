<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6 max-w-3xl mx-auto">
      <h2 class="text-2xl font-bold mb-4">Submit New Publication</h2>
      <div class="space-y-4">
        <label class="block">
          <div class="text-sm text-slate-600 mb-1">Title</div>
          <input
            v-model="title"
            placeholder="Title"
            class="w-full border rounded px-3 py-2"
          />
          <div v-if="validationErrors.title" class="text-red-600 text-sm mt-1">
            {{ validationErrors.title }}
          </div>
        </label>

        <div class="block">
          <div class="text-sm text-slate-600 mb-1">Authors</div>
          <div class="flex flex-wrap gap-2 mb-2">
            <span
              v-for="(a, i) in selectedAuthors"
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

        <div class="flex gap-4 items-center">
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">Year</div>
            <input
              v-model="year"
              type="number"
              class="w-32 border rounded px-3 py-2"
            />
          </label>
          <label class="flex-1">
            <div class="text-sm text-slate-600 mb-1">File</div>
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
            <div class="text-sm text-slate-600 mb-1">Type</div>
            <select v-model="type" class="w-full border rounded px-3 py-2">
              <option value="">-- Select --</option>
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
            <div class="text-sm text-slate-600 mb-1">Scientific Area</div>
            <select
              v-model="areaScientific"
              class="w-full border rounded px-3 py-2"
            >
              <option value="">-- Select --</option>
              <option v-for="a in scientificAreas" :key="a.id" :value="a.name">
                {{ a.name }}
              </option>
            </select>
            <div
              v-if="scientificAreas.length === 0"
              class="text-yellow-600 text-sm mt-1"
            >
              No scientific areas available — ask an administrator to create one.
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
          <div class="text-sm text-slate-600 mb-1">Abstract</div>
          <textarea
            v-model="abstract"
            placeholder="Abstract"
            class="w-full border rounded px-3 py-2"
            rows="6"
          ></textarea>
        </label>

        <label class="block">
          <div
            class="text-sm text-slate-600 mb-1 flex items-center justify-between"
          >
            <div>
              AI Generated Summary
              <span class="text-xs text-slate-400"
                >(optional - will be generated automatically or you can enter manually)</span
              >
            </div>
            <button
              @click="generateAISummary"
              :disabled="isGeneratingSummary || !title || !abstract"
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
              <span>{{
                isGeneratingSummary ? "Generating..." : "Generate AI Summary"
              }}</span>
            </button>
          </div>
          <textarea
            v-model="aiGeneratedSummary"
            placeholder="Automatically generated AI summary (can be edited/corrected if necessary)"
            class="w-full border rounded px-3 py-2 bg-blue-50"
            :rows="aiGeneratedSummary && aiGeneratedSummary.length > 200 ? 8 : 4"
          ></textarea>
          <div v-if="aiError" class="text-red-600 text-sm mt-1">
            {{ aiError }}
          </div>
        </label>

        <label class="block">
          <div class="text-sm text-slate-600 mb-1">
            Tags
            <span class="text-xs text-slate-400"
              >(optional - select one or more)</span
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
            <option value="">-- Select tag --</option>
            <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">
              {{ tag.name }}
            </option>
          </select>
          <div
            v-if="availableTags.length === 0"
            class="text-yellow-600 text-sm mt-1"
          >
            No tags available — ask a manager to create one.
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
              <div class="font-medium text-slate-800">Visible</div>
              <div class="text-xs text-slate-500">
                If unchecked, the publication will be hidden from the public
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
              <div class="font-medium text-slate-800">Confidential</div>
              <div class="text-xs text-slate-500">
                Only administrators and managers will be able to see
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
            <span>{{ isSubmitting ? "Submitting..." : "Submit" }}</span>
          </button>
          <NuxtLink to="/" class="px-4 py-2 border rounded">Cancel</NuxtLink>
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
const isGeneratingSummary = ref(false);
const aiError = ref(null);

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
      fileError.value = "Only PDF or ZIP files are allowed";
      file.value = null;
    }
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

const addAuthorFromSuggestion = (s) => {
  if (!selectedAuthors.value.includes(s.name)) selectedAuthors.value.push(s.name);
  authorQuery.value = "";
  suggestions.value = [];
};

const removeAuthor = (i) => {
  selectedAuthors.value.splice(i, 1);
};

const addRawAuthor = () => {
  const v = authorQuery.value && authorQuery.value.trim();
  if (!v) return;
  if (!selectedAuthors.value.includes(v)) selectedAuthors.value.push(v);
  authorQuery.value = "";
  suggestions.value = [];
};

const generateAISummary = async () => {
  aiError.value = null;

  // Validate that we have title and abstract
  if (!title.value || !title.value.trim()) {
    aiError.value = "Please fill in the title first";
    return;
  }

  if (!abstract.value || !abstract.value.trim()) {
    aiError.value = "Please fill in the abstract first";
    return;
  }

  isGeneratingSummary.value = true;

  try {
    const response = await api.post("/ai/generate-summary", {
      title: title.value,
      abstract: abstract.value,
    });

    if (response.data && response.data.summary) {
      aiGeneratedSummary.value = response.data.summary;
    } else {
      aiError.value = "Invalid server response";
    }
  } catch (e) {
    console.error("Error generating AI summary:", e);
    if (e?.response?.data?.message) {
      aiError.value = e.response.data.message;
    } else if (e?.response?.status === 503) {
      aiError.value =
        "AI service unavailable. Make sure Ollama is running.";
    } else {
      aiError.value = "Error generating summary. Please try again.";
    }
  } finally {
    isGeneratingSummary.value = false;
  }
};

const submit = async () => {
  error.value = null;
  validationErrors.value = {};
  if (!auth.token.value) return navigateTo("/auth/login");

  // Client-side validation
  if (!title.value || !title.value.trim())
    validationErrors.value.title = "Title is required";
  if (!type.value) validationErrors.value.type = "Type is required";
  if (!areaScientific.value)
    validationErrors.value.areaScientific = "Scientific area is required";
  if (!year.value) validationErrors.value.year = "Year is required";
  if (scientificAreas.value.length === 0) {
    error.value =
      "No scientific areas defined in the system. Contact an administrator.";
    return;
  }
  if (Object.keys(validationErrors.value).length > 0) {
    error.value = "Missing required fields";
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
        error.value = "Error submitting (400). See console for details.";
      }
    } else {
      error.value = "Error submitting (no server response)";
    }
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  if (!auth.token.value) {
    return navigateTo("/auth/login");
  }
  fetchAuxData();
});
</script>
