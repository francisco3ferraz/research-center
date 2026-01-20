<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div v-if="!isEditRoute && loading" class="text-slate-600">
        A carregar publicação...
      </div>
      <div
        v-else-if="!isEditRoute"
        class="grid grid-cols-1 lg:grid-cols-3 gap-6"
      >
        <div class="lg:col-span-2">
          <h2 class="text-2xl font-bold text-slate-800">{{ pub.title }}</h2>
          <div class="text-sm text-slate-500 mt-1">
            {{ (pub.authors || []).join(", ") }} — {{ pub.year }}
          </div>
          <div class="mt-3 flex items-center gap-2 flex-wrap">
            <span
              v-for="t in pub.tags || []"
              :key="t.id"
              class="px-2 py-1 bg-gray-100 text-slate-800 rounded-full text-sm flex items-center gap-2"
            >
              {{ t.name }}
              <button
                v-if="auth.token.value"
                @click="toggleTagSubscription(t)"
                class="text-xs text-blue-600 hover:underline"
              >
                {{ isSubscribed(t.id) ? "Cancelar subscrição" : "Subscrever" }}
              </button>
            </span>
          </div>
          <div
            class="mt-4 prose max-w-none text-slate-700"
            v-html="pub.abstract"
          ></div>

          <div
            v-if="pub.aiGeneratedSummary"
            class="mt-4 p-4 bg-blue-50 border-l-4 border-blue-400 rounded"
          >
            <h3 class="text-sm font-semibold text-blue-900 mb-2">
              📝 Resumo Gerado por IA
            </h3>
            <div class="text-sm text-slate-700">
              {{ pub.aiGeneratedSummary }}
            </div>
          </div>

          <div class="mt-4 flex gap-2 items-center">
            <button
              v-if="pub.documentId"
              @click="previewPdf"
              :disabled="isFetchingPdf"
              class="px-3 py-1 bg-blue-600 text-white rounded"
            >
              Ver PDF
            </button>
            <button
              v-if="pub.documentId"
              @click="downloadPdf"
              :disabled="isFetchingPdf"
              class="px-3 py-1 border rounded"
            >
              Descarregar
            </button>
            <button
              v-if="canEdit"
              @click="goEdit"
              class="px-3 py-1 border rounded text-sm text-blue-600"
            >
              Editar
            </button>
            <div v-if="isFetchingPdf" class="text-sm text-slate-500">
              A obter ficheiro...
            </div>
          </div>

          <div v-if="pdfUrl" class="mt-4">
            <iframe
              :src="pdfUrl"
              style="width: 100%; height: 80vh; border: 1px solid #e5e7eb"
            ></iframe>
          </div>

          <div class="mt-6">
            <h3 class="text-lg font-semibold">Comentários</h3>
            <div class="mt-3 space-y-3">
              <div
                v-for="c in displayedComments"
                :key="c.id"
                class="border rounded p-3 bg-gray-50"
              >
                  <div class="flex items-center gap-2">
                    <div class="font-medium">
                        {{ c.author?.name || "Anónimo" }}
                    </div>
                     <span v-if="c.visible === false" class="text-xs bg-red-100 text-red-800 px-2 py-0.5 rounded font-bold">Oculto</span>
                  </div>
                  <div class="flex items-center gap-2">
                      <div class="text-xs text-slate-400">
                        {{
                        c.createdAt ? new Date(c.createdAt).toLocaleString() : ""
                        }}
                      </div>
                      <button v-if="canManageComments" @click="toggleCommentVisibility(c)" class="text-xs text-blue-600 hover:underline font-medium">
                        {{ c.visible ? 'Ocultar' : 'Mostrar' }}
                      </button>
                  </div>
                <div class="mt-2 text-slate-700">
                  {{ c.content || c.text || "" }}
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between mt-4">
              <div>
                <button
                  @click="commentsPrev"
                  :disabled="commentsPage <= 0"
                  class="px-3 py-1 border rounded bg-white"
                >
                  Anterior
                </button>
                <button
                  @click="commentsNext"
                  :disabled="commentsPage + 1 >= totalCommentsPages"
                  class="ml-2 px-3 py-1 border rounded bg-white"
                >
                  Seguinte
                </button>
              </div>
              <div class="text-sm text-slate-600">
                Página {{ commentsPage + 1 }} / {{ totalCommentsPages }}
              </div>
            </div>

            <div v-if="auth.token.value" class="mt-4">
              <textarea
                v-model="newComment"
                class="w-full border p-3 rounded"
                placeholder="Adicionar comentário"
              ></textarea>
              <div class="mt-2 text-right">
                <button
                  @click="postComment"
                  class="bg-blue-600 text-white px-4 py-2 rounded"
                >
                  Enviar comentário
                </button>
              </div>
            </div>
            <div v-else class="text-sm text-slate-500 mt-3">
              Faça login para comentar.
            </div>
          </div>

          <div
            v-if="canEdit || auth.user.value?.role === 'ADMINISTRADOR'"
            class="mt-6"
          >
            <h3 class="text-lg font-semibold">Histórico de Edições</h3>
            <div v-if="loadingHistory" class="text-sm text-slate-500 mt-2">
              A carregar histórico...
            </div>
            <div
              v-else-if="history.length === 0"
              class="text-sm text-slate-500 mt-2"
            >
              Sem histórico de edições
            </div>
            <div v-else class="mt-3 space-y-2">
              <div
                v-for="h in history"
                :key="h.id"
                class="border-l-4 border-blue-400 pl-3 py-2 bg-gray-50"
              >
                <div class="flex justify-between items-start">
                  <div>
                    <div class="text-sm font-medium">
                      {{ h.actionType }} por {{ h.editedBy?.name || h.editedBy?.username || 'Sistema' }}
                    </div>
                    <div class="text-xs text-slate-500">
                      {{ h.description }}
                    </div>
                    <div
                      v-if="h.changedFields"
                      class="text-xs text-slate-400 mt-1"
                    >
                      Campos: {{ h.changedFields }}
                    </div>
                  </div>
                  <div class="text-xs text-slate-400 whitespace-nowrap">
                    {{
                      h.timestamp ? new Date(h.timestamp).toLocaleString() : ""
                    }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <aside class="bg-white p-4 border rounded-lg">
          <div class="text-center">
            <div class="text-3xl font-bold">
              {{ ratings.averageRating?.toFixed(2) || "—" }}
            </div>
            <div class="text-sm text-slate-500">
              Média • {{ ratings.totalRatings || 0 }} avaliações
            </div>
          </div>

          <div class="mt-4">
            <div class="flex justify-center gap-2">
              <button
                v-for="n in 5"
                :key="n"
                @click="rate(n)"
                class="px-3 py-1 border rounded bg-white"
              >
                {{ n }}
              </button>
            </div>
          </div>

          <div class="mt-4">
            <h4 class="font-medium">Avaliações</h4>
            <div class="mt-2 space-y-2">
              <div
                v-for="r in displayedRatings"
                :key="r.id"
                class="flex justify-between items-center border rounded p-2 bg-gray-50"
              >
                <div class="text-sm">
                  <strong>{{ r.userName }}</strong>
                </div>
                <div class="text-sm">{{ r.value }}</div>
              </div>
            </div>
            <div class="flex items-center justify-between mt-3">
              <button
                @click="ratingsPrev"
                :disabled="ratingsPage <= 0"
                class="px-3 py-1 border rounded bg-white"
              >
                Anterior
              </button>
              <div class="text-sm text-slate-600">
                {{ ratingsPage + 1 }} / {{ totalRatingsPages }}
              </div>
              <button
                @click="ratingsNext"
                :disabled="ratingsPage + 1 >= totalRatingsPages"
                class="px-3 py-1 border rounded bg-white"
              >
                Seguinte
              </button>
            </div>
          </div>
        </aside>
      </div>
      <NuxtPage v-else />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onBeforeUnmount } from "vue";
const route = useRoute();
const id = route.params.id;
const api = useApi();
const auth = useAuth();

const pub = ref({});
const loading = ref(true);
const comments = ref([]);
const newComment = ref("");
const ratings = ref({});
const pdfUrl = ref(null);
const isFetchingPdf = ref(false);
const subscribedTagIds = ref(new Set());
const history = ref([]);
const loadingHistory = ref(false);

// Pagination state
const pageSize = 10;
const commentsPage = ref(0);
const ratingsPage = ref(0);

const ratingsList = computed(() =>
  ratings.value && Array.isArray(ratings.value.ratings)
    ? ratings.value.ratings
    : [],
);
const commentsList = computed(() =>
  Array.isArray(comments.value) ? comments.value : [],
);

const totalCommentsPages = computed(() =>
  Math.max(1, Math.ceil(commentsList.value.length / pageSize)),
);
const totalRatingsPages = computed(() =>
  Math.max(1, Math.ceil(ratingsList.value.length / pageSize)),
);

const displayedComments = computed(() => {
  const start = commentsPage.value * pageSize;
  return commentsList.value.slice(start, start + pageSize);
});

const displayedRatings = computed(() => {
  const start = ratingsPage.value * pageSize;
  return ratingsList.value.slice(start, start + pageSize);
});

const load = async () => {
  loading.value = true;
  try {
    const resp = await api.get(`/publications/${id}`);
    pub.value = resp.data;
    if (auth.token.value) await loadSubscriptions();
    const cResp = await api.get(`/publications/${id}/comments`);
    comments.value = cResp.data || [];
    const rResp = await api.get(`/publications/${id}/ratings`);
    ratings.value = rResp.data || {};
    // Load history if user can edit
    if (auth.token.value) await loadHistory();
    // reset pages if out of range
    if (commentsPage.value >= totalCommentsPages.value)
      commentsPage.value = Math.max(0, totalCommentsPages.value - 1);
    if (ratingsPage.value >= totalRatingsPages.value)
      ratingsPage.value = Math.max(0, totalRatingsPages.value - 1);
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

const loadSubscriptions = async () => {
  try {
    const resp = await api.get("/subscriptions/tags");
    const arr = resp.data || [];
    subscribedTagIds.value = new Set(arr.map((tag) => tag.id));
  } catch (e) {
    console.error("Failed to load subscriptions", e);
  }
};

const loadHistory = async () => {
  loadingHistory.value = true;
  try {
    const resp = await api.get(`/publications/${id}/history`);
    history.value = resp.data || [];
  } catch (e) {
    console.error("Failed to load history", e);
    history.value = [];
  } finally {
    loadingHistory.value = false;
  }
};

const isSubscribed = (tagId) => subscribedTagIds.value.has(tagId);

const toggleTagSubscription = async (tag) => {
  if (!auth.token.value) return navigateTo("/auth/login");
  try {
    if (isSubscribed(tag.id)) {
      await api.delete(`/subscriptions/tags/${tag.id}`);
      subscribedTagIds.value.delete(tag.id);
    } else {
      await api.post("/subscriptions/tags", { tagId: tag.id });
      subscribedTagIds.value.add(tag.id);
    }
    // force reactivity
    subscribedTagIds.value = new Set(subscribedTagIds.value);
  } catch (e) {
    console.error("Subscription error", e);
    alert(e?.response?.data?.message || "Erro ao atualizar subscrição");
  }
};

const previewPdf = async () => {
  if (!pub.value || !pub.value.documentId) return;
  if (pdfUrl.value) return; // já carregado
  isFetchingPdf.value = true;
  try {
    const resp = await api.get(`/publications/${id}/file`, {
      responseType: "blob",
    });
    // Force MIME type to application/pdf so browsers render in iframe
    const blob = new Blob([resp.data], { type: "application/pdf" });
    pdfUrl.value = URL.createObjectURL(blob);
  } catch (e) {
    console.error("Erro ao obter ficheiro", e);
    alert("Não foi possível obter o ficheiro. Verifique se tem permissões.");
  } finally {
    isFetchingPdf.value = false;
  }
};

const downloadPdf = async () => {
  if (!pub.value || !pub.value.documentId) return;
  isFetchingPdf.value = true;
  try {
    const resp = await api.get(`/publications/${id}/file`, {
      responseType: "blob",
    });
    const blob = new Blob([resp.data], { type: "application/pdf" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = pub.value.documentFilename || "file.pdf";
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  } catch (e) {
    console.error("Erro ao descarregar ficheiro", e);
    alert("Não foi possível descarregar o ficheiro.");
  } finally {
    isFetchingPdf.value = false;
  }
};

onBeforeUnmount(() => {
  if (pdfUrl.value) {
    URL.revokeObjectURL(pdfUrl.value);
    pdfUrl.value = null;
  }
});

const postComment = async () => {
  if (!newComment.value || newComment.value.trim() === "") return;
  try {
    await api.post(`/publications/${id}/comments`, {
      content: newComment.value,
    });
    newComment.value = "";
    // reload and jump to last page where new comment likely resides
    await load();
    commentsPage.value = totalCommentsPages.value - 1;
  } catch (e) {
    console.error(e);
  }
};

const rate = async (value) => {
  if (!auth.token.value) return navigateTo("/auth/login");
  try {
    await api.post(`/publications/${id}/ratings`, { value });
    const rResp = await api.get(`/publications/${id}/ratings`);
    ratings.value = rResp.data || {};
    // go to last page to show user's rating
    ratingsPage.value = totalRatingsPages.value - 1;
  } catch (e) {
    console.error(e);
  }
};

const commentsPrev = () => {
  if (commentsPage.value > 0) commentsPage.value--;
};
const commentsNext = () => {
  if (commentsPage.value + 1 < totalCommentsPages.value) commentsPage.value++;
};
const ratingsPrev = () => {
  if (ratingsPage.value > 0) ratingsPage.value--;
};
const ratingsNext = () => {
  if (ratingsPage.value + 1 < totalRatingsPages.value) ratingsPage.value++;
};

const goEdit = () => {
  try {
    // Force full page navigation to ensure the edit route is loaded
    window.location.href = `/publications/${id}/edit`;
  } catch (e) {
    console.error("Navigation error", e);
  }
};

onMounted(load);

const isEditRoute = computed(() => (route.path || "").endsWith("/edit"));

const canEdit = computed(() => {
  if (!auth.token.value || !auth.user.value) return false;
  if (
    auth.user.value.role === "ADMINISTRADOR" ||
    auth.user.value.role === "RESPONSAVEL"
  )
    return true;
  return (
    pub.value.uploadedBy &&
    auth.user.value.id &&
    pub.value.uploadedBy.id === auth.user.value.id
  );
});

const canManageComments = computed(() => {
  if (!auth.token.value || !auth.user.value) return false;
  return auth.user.value.role === 'ADMINISTRADOR' || auth.user.value.role === 'RESPONSAVEL';
});

const toggleCommentVisibility = async (c) => {
    try {
        await api.patch(`/comments/${c.id}/visibility`, { visible: !c.visible });
        c.visible = !c.visible; 
    } catch(e) { console.error(e); alert('Erro ao alterar visibilidade'); }
};
</script>
