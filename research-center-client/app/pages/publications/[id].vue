<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div v-if="!isEditRoute && loading" class="text-slate-600">
        Loading publication...
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
                {{ isSubscribed(t.id) ? "Unsubscribe" : "Subscribe" }}
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
              📝 AI Generated Summary
            </h3>
            <div class="text-sm text-slate-700 relative">
              <div class="whitespace-pre-wrap">
                {{
                  isSummaryExpanded
                    ? pub.aiGeneratedSummary
                    : pub.aiGeneratedSummary.length > 100
                      ? pub.aiGeneratedSummary.substring(0, 100) + "..."
                      : pub.aiGeneratedSummary
                }}
              </div>
              <button
                v-if="pub.aiGeneratedSummary.length > 100"
                @click="isSummaryExpanded = !isSummaryExpanded"
                class="mt-2 text-xs font-semibold text-blue-700 hover:text-blue-900 focus:outline-none flex items-center gap-1"
              >
                {{ isSummaryExpanded ? "Minimize" : "Read more" }}
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-3 w-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  :class="{ 'rotate-180': isSummaryExpanded }"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </button>
            </div>
          </div>

          <div class="mt-4 flex gap-2 items-center">
            <button
              v-if="pub.documentId && !pdfUrl"
              @click="previewPdf"
              :disabled="isFetchingPdf"
              class="px-3 py-1 bg-blue-600 text-white rounded"
            >
              View PDF
            </button>
            <button
              v-if="pub.documentId && pdfUrl"
              @click="pdfUrl = null"
              class="px-3 py-1 bg-red-600 text-white rounded"
            >
              Close PDF
            </button>
            <button
              v-if="pub.documentId"
              @click="downloadPdf"
              :disabled="isFetchingPdf"
              class="px-3 py-1 border rounded"
            >
              Download
            </button>
            <button
              v-if="canEdit"
              @click="goEdit"
              class="px-3 py-1 border rounded text-sm text-blue-600"
            >
              Edit
            </button>
            <button
              v-if="canManagePublication"
              @click="togglePublicationVisibility"
              class="px-3 py-1 border rounded text-sm"
              :class="pub.visible ? 'text-orange-600 border-orange-300' : 'text-green-600 border-green-300'"
            >
              {{ pub.visible ? 'Hide' : 'Show' }}
            </button>
            <button
              v-if="canManagePublication"
              @click="deletePublication"
              class="px-3 py-1 border border-red-300 rounded text-sm text-red-600 hover:bg-red-50"
            >
              Delete
            </button>
            <div v-if="isFetchingPdf" class="text-sm text-slate-500">
              Fetching file...
            </div>
          </div>

          <div v-if="pdfUrl" class="mt-4">
            <iframe
              :src="pdfUrl"
              style="width: 100%; height: 80vh; border: 1px solid #e5e7eb"
            ></iframe>
          </div>

          <div class="mt-6">
            <h3 class="text-lg font-semibold">Comments</h3>
            <div class="mt-3 space-y-3">
              <div
                v-for="c in displayedComments"
                :key="c.id"
                class="border rounded p-3 bg-gray-50"
              >
                <div class="flex items-center gap-2">
                  <div class="font-medium">
                    {{ c.author?.name || "Anonymous" }}
                  </div>
                  <span
                    v-if="c.visible === false"
                    class="text-xs bg-red-100 text-red-800 px-2 py-0.5 rounded font-bold"
                    >Hidden</span
                  >
                </div>
                <div class="flex items-center gap-2">
                  <div class="text-xs text-slate-400">
                    {{
                      c.createdAt ? new Date(c.createdAt).toLocaleString() : ""
                    }}
                  </div>
                  <button
                    v-if="canManageComments"
                    @click="toggleCommentVisibility(c)"
                    class="text-xs text-blue-600 hover:underline font-medium"
                  >
                    {{ c.visible ? "Hide" : "Show" }}
                  </button>
                  <button
                    v-if="canEditComment(c)"
                    @click="startEditComment(c)"
                    class="text-xs text-green-600 hover:underline font-medium"
                  >
                    Edit
                  </button>
                  <button
                    v-if="canDeleteComment(c)"
                    @click="deleteComment(c)"
                    class="text-xs text-red-600 hover:underline font-medium"
                  >
                    Delete
                  </button>
                </div>

                <!-- Edit Mode -->
                <div v-if="editingCommentId === c.id" class="mt-2">
                  <textarea
                    v-model="editCommentContent"
                    class="w-full border p-2 rounded text-sm"
                    rows="3"
                  ></textarea>
                  <div class="mt-2 flex gap-2">
                    <button
                      @click="saveEditComment(c)"
                      class="px-3 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700"
                    >
                      Save
                    </button>
                    <button
                      @click="cancelEditComment()"
                      class="px-3 py-1 border rounded text-sm hover:bg-gray-100"
                    >
                      Cancel
                    </button>
                  </div>
                </div>

                <!-- Normal View -->
                <div v-else class="mt-2 text-slate-700">
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
                  Previous
                </button>
                <button
                  @click="commentsNext"
                  :disabled="commentsPage + 1 >= totalCommentsPages"
                  class="ml-2 px-3 py-1 border rounded bg-white"
                >
                  Next
                </button>
              </div>
              <div class="text-sm text-slate-600">
                Page {{ commentsPage + 1 }} / {{ totalCommentsPages }}
              </div>
            </div>

            <div v-if="auth.token.value" class="mt-4">
              <textarea
                v-model="newComment"
                class="w-full border p-3 rounded"
                placeholder="Add comment"
              ></textarea>
              <div class="mt-2 text-right">
                <button
                  @click="postComment"
                  class="bg-blue-600 text-white px-4 py-2 rounded"
                >
                  Post comment
                </button>
              </div>
            </div>
            <div v-else class="text-sm text-slate-500 mt-3">
              Log in to comment.
            </div>
          </div>

          <div
            v-if="canEdit || auth.user.value?.role === 'ADMINISTRADOR'"
            class="mt-6"
          >
            <h3 class="text-lg font-semibold">Edit History</h3>
            <div v-if="loadingHistory" class="text-sm text-slate-500 mt-2">
              Loading history...
            </div>
            <div
              v-else-if="history.length === 0"
              class="text-sm text-slate-500 mt-2"
            >
              No edit history
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
                      {{ h.actionType }} por
                      {{
                        h.editedBy?.name || h.editedBy?.username || "Sistema"
                      }}
                    </div>
                    <div class="text-xs text-slate-500">
                      {{ h.description }}
                    </div>
                    <div
                      v-if="h.changedFields"
                      class="text-xs text-slate-400 mt-1"
                    >
                      Fields: {{ h.changedFields }}
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
              Average • {{ ratings.totalRatings || 0 }} ratings
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
            <div v-if="hasMyRating" class="mt-2 text-center">
              <button
                @click="removeMyRating"
                class="text-xs text-red-600 hover:underline"
              >
                Remove my rating
              </button>
            </div>
          </div>

          <div class="mt-4">
            <h4 class="font-medium">Ratings</h4>
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
                Previous
              </button>
              <div class="text-sm text-slate-600">
                {{ ratingsPage + 1 }} / {{ totalRatingsPages }}
              </div>
              <button
                @click="ratingsNext"
                :disabled="ratingsPage + 1 >= totalRatingsPages"
                class="px-3 py-1 border rounded bg-white"
              >
                Next
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
import { ref, onMounted, computed, onBeforeUnmount, watch } from "vue";
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
const isSummaryExpanded = ref(false);

// Edit comment state
const editingCommentId = ref(null);
const editCommentContent = ref("");

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
    alert(e?.response?.data?.message || "Error updating subscription");
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
    console.error("Error fetching file", e);
    alert("Could not fetch file. Check if you have permissions.");
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
    console.error("Error downloading file", e);
    alert("Could not download file.");
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

watch(isEditRoute, (isEdit) => {
  if (!isEdit) {
    load();
  }
});

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
  return (
    auth.user.value.role === "ADMINISTRADOR" ||
    auth.user.value.role === "RESPONSAVEL"
  );
});

const toggleCommentVisibility = async (c) => {
  try {
    await api.patch(`/comments/${c.id}/visibility`, { visible: !c.visible });
    c.visible = !c.visible;
  } catch (e) {
    console.error(e);
    alert("Error changing visibility");
  }
};

// Edit comment functions
const canEditComment = (c) => {
  if (!auth.token.value || !auth.user.value) return false;
  return c.author && c.author.id === auth.user.value.id;
};

const startEditComment = (c) => {
  editingCommentId.value = c.id;
  editCommentContent.value = c.content || c.text || "";
};

const cancelEditComment = () => {
  editingCommentId.value = null;
  editCommentContent.value = "";
};

const saveEditComment = async (c) => {
  if (!editCommentContent.value.trim()) {
    alert("Comment cannot be empty");
    return;
  }
  try {
    await api.put(`/comments/${c.id}`, { content: editCommentContent.value });
    c.content = editCommentContent.value;
    c.text = editCommentContent.value;
    cancelEditComment();
  } catch (e) {
    console.error(e);
    alert("Error saving comment");
  }
};

// EP19 & EP20: Manage publication (delete, hide/show) - for admin/responsavel
const canManagePublication = computed(() => {
  if (!auth.token.value || !auth.user.value) return false;
  return (
    auth.user.value.role === "ADMINISTRADOR" ||
    auth.user.value.role === "RESPONSAVEL"
  );
});

const togglePublicationVisibility = async () => {
  if (!confirm(`Are you sure you want to ${pub.value.visible ? 'hide' : 'show'} this publication?`)) return;
  try {
    // Backend uses POST for visibility toggle (EP20)
    await api.post(`/publications/${id}/visibility`, { visible: !pub.value.visible });
    pub.value.visible = !pub.value.visible;
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error changing publication visibility");
  }
};

const deletePublication = async () => {
  if (!confirm("Are you sure you want to DELETE this publication? This action cannot be undone.")) return;
  try {
    await api.delete(`/publications/${id}`);
    alert("Publication deleted successfully");
    navigateTo("/");
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error deleting publication");
  }
};

// EP31: Delete own comment
const canDeleteComment = (c) => {
  if (!auth.token.value || !auth.user.value) return false;
  // User can delete their own comments, or admin/responsavel can delete any
  return (
    (c.author && c.author.id === auth.user.value.id) ||
    auth.user.value.role === "ADMINISTRADOR" ||
    auth.user.value.role === "RESPONSAVEL"
  );
};

const deleteComment = async (c) => {
  if (!confirm("Are you sure you want to delete this comment?")) return;
  try {
    await api.delete(`/comments/${c.id}`);
    comments.value = comments.value.filter(comment => comment.id !== c.id);
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error deleting comment");
  }
};

// EP27: Remove own rating
const hasMyRating = computed(() => {
  if (!auth.token.value || !auth.user.value) return false;
  return ratingsList.value.some(r => r.userId === auth.user.value.id);
});

const removeMyRating = async () => {
  if (!confirm("Are you sure you want to remove your rating?")) return;
  try {
    await api.delete(`/publications/${id}/ratings`);
    // Refresh ratings
    const rResp = await api.get(`/publications/${id}/ratings`);
    ratings.value = rResp.data || {};
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error removing rating");
  }
};
</script>
