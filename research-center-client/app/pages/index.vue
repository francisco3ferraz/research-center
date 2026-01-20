<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-3xl font-bold text-slate-800">Research Center</h1>
        <p class="text-sm text-slate-500">
          Explore publications, comment and rate.
        </p>
      </div>
      <div class="flex items-center gap-3">
        <NuxtLink
          v-if="auth.token.value"
          to="/publications/my"
          class="border border-blue-600 text-blue-600 px-4 py-2 rounded shadow hover:bg-blue-50"
          >My Publications</NuxtLink
        >
        <NuxtLink
          v-if="auth.token.value"
          to="/publications/create"
          class="bg-green-600 text-white px-4 py-2 rounded shadow hover:bg-green-700"
          >New Publication</NuxtLink
        >
        <NuxtLink
          to="/auth/login"
          v-if="!auth.token.value"
          class="text-blue-600"
          >Login</NuxtLink
        >
      </div>
    </div>

    <div class="bg-white shadow rounded-lg p-4">
      <div class="bg-slate-50 border rounded-lg p-4 mb-6">
        <h3 class="text-sm font-semibold text-slate-700 mb-3">Filters</h3>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          <!-- Text Search -->
          <div class="col-span-1 md:col-span-2 lg:col-span-1">
            <label class="block text-xs text-slate-500 mb-1">Search</label>
            <input
              v-model="search"
              placeholder="Title, author or tag..."
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
          </div>

          <!-- Scientific Area -->
          <div>
             <label class="block text-xs text-slate-500 mb-1">Scientific Area</label>
             <select
              v-model="selectedArea"
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            >
              <option value="">All Areas</option>
              <option v-for="area in scientificAreas" :key="area.id" :value="area.name">
                {{ area.name }}
              </option>
            </select>
          </div>

          <!-- Tag -->
          <div>
             <label class="block text-xs text-slate-500 mb-1">Tag</label>
            <select
              v-model="selectedTag"
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            >
              <option value="">All Tags</option>
              <option v-for="tag in availableTags" :key="tag.id" :value="tag.id">
                {{ tag.name }}
              </option>
            </select>
          </div>

          <!-- Sort -->
          <div>
            <label class="block text-xs text-slate-500 mb-1">Sort By</label>
            <select
              v-model="sortBy"
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            >
              <option value="">Default (Newest)</option>
              <option value="comments">Most Comments</option>
              <option value="rating">Highest Rating</option>
              <option value="ratings_count">Most Ratings</option>
              <option value="views">Most Views</option>
              <option value="date">Upload Date</option>
            </select>
          </div>

          <!-- Date From -->
           <div>
            <label class="block text-xs text-slate-500 mb-1">From Date</label>
            <input
              type="date"
              v-model="dateFrom"
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
          </div>

          <!-- Date To -->
           <div>
            <label class="block text-xs text-slate-500 mb-1">To Date</label>
            <input
              type="date"
              v-model="dateTo"
              class="w-full border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
          </div>

          <!-- Checkbox -->
           <div class="flex items-end pb-2">
            <div v-if="auth.token.value && (auth.user.value?.role === 'ADMINISTRADOR' || auth.user.value?.role === 'RESPONSAVEL')" class="flex items-center gap-2">
                <input type="checkbox" v-model="showHidden" id="showHidden" class="w-4 h-4 text-blue-600 rounded">
                <label for="showHidden" class="text-sm select-none cursor-pointer text-slate-700">Show Hidden</label>
            </div>
          </div>

          <!-- Buttons -->
          <div class="flex gap-2 items-end">
            <button
              @click="fetchPubs"
              class="flex-1 bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 transition-colors"
            >
              Search
            </button>
            <button @click="clearFilters" class="px-3 py-2 border rounded hover:bg-gray-50 text-slate-600 transition-colors" title="Clear Filters">
               ✕
            </button>
            <button 
              v-if="auth.token.value" 
              @click="exportCsv" 
              class="px-3 py-2 border border-green-600 text-green-600 rounded hover:bg-green-50 flex items-center justify-center transition-colors"
              title="Export to CSV"
            >
              📥
            </button>
          </div>
        </div>
      </div>

      <div v-if="loading" class="text-slate-600">Loading...</div>
      <div v-else>
        <div v-if="items.length === 0" class="text-gray-500">
          No publications found.
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="p in items"
            :key="p.id"
            class="border rounded-lg p-4 hover:shadow-lg transition"
          >
            <NuxtLink
              :to="`/publications/${p.id}`"
              class="text-lg text-slate-800 font-semibold hover:text-blue-600"
              >{{ p.title }}</NuxtLink
            >
            <div class="text-sm text-slate-500 mt-1">
              {{ (p.authors || []).join(", ") }} — {{ p.year || "—" }}
            </div>
            <div
              class="flex items-center justify-between mt-3 text-sm text-slate-600"
            >
              <div>
                ⭐ {{ p.averageRating?.toFixed?.(2) || "—" }} • 💬
                {{ p.commentsCount || 0 }}
              </div>
              <div class="text-xs text-slate-400">
                {{ p.viewsCount || 0 }} views
              </div>
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mt-6">
          <div class="text-sm text-slate-600">
            Page {{ page + 1 }} of {{ totalPages }}
          </div>
          <div class="flex gap-2">
            <button
              :disabled="page <= 0"
              @click="prevPage"
              class="px-3 py-1 border rounded bg-white"
            >
              Previous
            </button>
            <button
              :disabled="page + 1 >= totalPages"
              @click="nextPage"
              class="px-3 py-1 border rounded bg-white"
            >
              Next
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
const auth = useAuth();
const api = useApi();

const items = ref([]);
const loading = ref(false);
const search = ref("");
const sortBy = ref("");
const order = ref("desc");
const page = ref(0);
const size = ref(10);
const totalPages = ref(1);
const showHidden = ref(false);

// Advanced filters
const scientificAreas = ref([]);
const availableTags = ref([]);
const selectedArea = ref("");
const selectedTag = ref("");
const dateFrom = ref("");
const dateTo = ref("");

const fetchAuxData = async () => {
  try {
    const [areasResp, tagsResp] = await Promise.all([
      api.get("/scientific-areas"),
      api.get("/tags"),
    ]);
    scientificAreas.value = areasResp.data || [];
    availableTags.value = (tagsResp.data || []).filter((t) => t.visible);
  } catch (e) {
    console.warn("Failed to fetch auxiliary data for filters", e);
  }
};

const fetchPubs = async () => {
  loading.value = true;
  try {
    const params = {
      search: search.value,
      areaScientific: selectedArea.value || undefined,
      tag: selectedTag.value || undefined,
      dateFrom: dateFrom.value ? new Date(dateFrom.value).toISOString() : undefined,
      dateTo: dateTo.value ? new Date(dateTo.value).toISOString() : undefined,
      page: page.value,
      size: size.value,
      sortBy: sortBy.value || undefined,
      order: order.value,
      showHidden: showHidden.value,
    };
    const resp = await api.get("/publications", { params });
    const data = resp.data;
    items.value = data.content || [];
    totalPages.value = data.totalPages || 1;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const exportCsv = async () => {
  if (!auth.token.value) return navigateTo('/auth/login');
  try {
     const params = {
      search: search.value,
      areaScientific: selectedArea.value || undefined,
      tag: selectedTag.value || undefined,
      dateFrom: dateFrom.value ? new Date(dateFrom.value).toISOString() : undefined,
      dateTo: dateTo.value ? new Date(dateTo.value).toISOString() : undefined,
      sortBy: sortBy.value || undefined,
      order: order.value,
      showHidden: showHidden.value,
      format: 'csv'
    };
    const resp = await api.get("/publications/export", { params, responseType: 'blob' });
    const blob = new Blob([resp.data], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'publications.csv';
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  } catch(e) {
    console.error("Export failed", e);
    alert("Failed to export publications.");
  }
};

const clearFilters = () => {
  search.value = "";
  selectedArea.value = "";
  selectedTag.value = "";
  dateFrom.value = "";
  dateTo.value = "";
  sortBy.value = "";
  order.value = "desc";
  showHidden.value = false;
  page.value = 0;
  fetchPubs();
};

const prevPage = () => {
  if (page.value > 0) {
    page.value--;
    fetchPubs();
  }
};
const nextPage = () => {
  if (page.value + 1 < totalPages.value) {
    page.value++;
    fetchPubs();
  }
};

onMounted(() => {
  fetchAuxData();
  fetchPubs();
});
</script>
