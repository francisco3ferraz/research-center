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
      <div class="grid grid-cols-1 md:grid-cols-4 gap-3 mb-4 items-end">
        <input
          v-model="search"
          placeholder="Search title, author or tag..."
          class="border rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-200"
        />

        <select
          v-model="sortBy"
          @change="fetchPubs"
          class="border rounded px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          <option value="">Sort by...</option>
          <option value="comments">Comments Count</option>
          <option value="rating">Average Rating</option>
          <option value="ratings_count">Ratings Count</option>
          <option value="views">Views Count</option>
          <option value="date">Upload Date</option>
        </select>

        <div v-if="auth.token.value && (auth.user.value?.role === 'ADMINISTRADOR' || auth.user.value?.role === 'RESPONSAVEL')" class="flex items-center gap-2 h-full pb-2">
            <input type="checkbox" v-model="showHidden" id="showHidden" class="w-4 h-4 text-blue-600 rounded">
            <label for="showHidden" class="text-sm select-none cursor-pointer">Show Hidden</label>
        </div>

        <div class="flex gap-2 w-full">
          <button
            @click="fetchPubs"
            class="flex-1 bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700"
          >
            Search
          </button>
          <button @click="clearFilters" class="px-4 py-2 border rounded hover:bg-gray-50">
            Clear
          </button>
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

const fetchPubs = async () => {
  loading.value = true;
  try {
    const params = {
      search: search.value,
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

const clearFilters = () => {
  search.value = "";
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

onMounted(() => fetchPubs());
</script>
