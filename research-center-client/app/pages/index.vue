<template>
  <div class="container mx-auto px-4 py-6">
    <div class="flex items-center justify-between mb-6">
      <div>
        <h1 class="text-3xl font-bold text-slate-800">Research Center</h1>
        <p class="text-sm text-slate-500">
          Explorar publicações, comentar e avaliar.
        </p>
      </div>
      <div class="flex items-center gap-3">
        <NuxtLink
          v-if="auth.token.value"
          to="/publications/my"
          class="border border-blue-600 text-blue-600 px-4 py-2 rounded shadow hover:bg-blue-50"
          >Minhas Publicações</NuxtLink
        >
        <NuxtLink
          v-if="auth.token.value"
          to="/publications/create"
          class="bg-green-600 text-white px-4 py-2 rounded shadow hover:bg-green-700"
          >Nova Publicação</NuxtLink
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
      <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
        <input
          v-model="search"
          placeholder="Pesquisar título, autor ou tag..."
          class="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-200"
        />

        <select
          v-model="sortBy"
          @change="fetchPubs"
          class="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          <option value="">Ordenar por...</option>
          <option value="comments">Nº Comentários</option>
          <option value="rating">Rating Médio</option>
          <option value="ratings_count">Nº Ratings</option>
          <option value="date">Data Upload</option>
        </select>

        <div class="flex gap-2">
          <button
            @click="fetchPubs"
            class="flex-1 bg-blue-600 text-white px-4 py-2 rounded shadow"
          >
            Pesquisar
          </button>
          <button @click="clearFilters" class="px-4 py-2 border rounded">
            Limpar
          </button>
        </div>
      </div>

      <div v-if="loading" class="text-slate-600">A carregar...</div>
      <div v-else>
        <div v-if="items.length === 0" class="text-gray-500">
          Nenhuma publicação encontrada.
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
                {{ p.viewsCount || 0 }} visualizações
              </div>
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mt-6">
          <div class="text-sm text-slate-600">
            Página {{ page + 1 }} de {{ totalPages }}
          </div>
          <div class="flex gap-2">
            <button
              :disabled="page <= 0"
              @click="prevPage"
              class="px-3 py-1 border rounded bg-white"
            >
              Anterior
            </button>
            <button
              :disabled="page + 1 >= totalPages"
              @click="nextPage"
              class="px-3 py-1 border rounded bg-white"
            >
              Seguinte
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

const fetchPubs = async () => {
  loading.value = true;
  try {
    const params = {
      search: search.value,
      page: page.value,
      size: size.value,
      sortBy: sortBy.value || undefined,
      order: order.value,
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
