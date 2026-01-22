<template>
  <div class="container mx-auto px-4 py-8">
    <div class="mb-6">
      <h1 class="text-3xl font-bold text-slate-800">Statistics</h1>
      <p class="text-slate-500 mt-1">Platform overview and your activity</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div
        class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"
      ></div>
      <p class="mt-2 text-gray-500">Loading statistics...</p>
    </div>

    <div v-else class="space-y-6">
      <!-- Overview Stats Cards -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <div class="bg-white rounded-lg shadow p-4 text-center">
          <div class="text-3xl font-bold text-blue-600">
            {{ overview.totalPublications || 0 }}
          </div>
          <div class="text-sm text-slate-500 mt-1">Publications</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4 text-center">
          <div class="text-3xl font-bold text-green-600">
            {{ overview.totalUsers || 0 }}
          </div>
          <div class="text-sm text-slate-500 mt-1">Users</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4 text-center">
          <div class="text-3xl font-bold text-purple-600">
            {{ overview.totalComments || 0 }}
          </div>
          <div class="text-sm text-slate-500 mt-1">Comments</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4 text-center">
          <div class="text-3xl font-bold text-yellow-600">
            {{ overview.totalRatings || 0 }}
          </div>
          <div class="text-sm text-slate-500 mt-1">Ratings</div>
        </div>
        <div class="bg-white rounded-lg shadow p-4 text-center">
          <div class="text-3xl font-bold text-pink-600">
            {{ overview.totalTags || 0 }}
          </div>
          <div class="text-sm text-slate-500 mt-1">Tags</div>
        </div>
      </div>

      <!-- Publications by Type & Area -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- By Type -->
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-slate-800 mb-4">
            Publications by Type
          </h2>
          <div
            v-if="Object.keys(overview.publicationsByType || {}).length === 0"
            class="text-slate-400 text-sm"
          >
            No data available
          </div>
          <div v-else class="space-y-3">
            <div
              v-for="(count, type) in overview.publicationsByType"
              :key="type"
              class="flex items-center gap-3"
            >
              <div
                class="w-28 text-sm text-slate-600 truncate"
                :title="formatTypeName(type)"
              >
                {{ formatTypeName(type) }}
              </div>
              <div class="flex-1 bg-gray-100 rounded-full h-4 overflow-hidden">
                <div
                  class="h-full bg-blue-500 rounded-full transition-all duration-500"
                  :style="{
                    width:
                      getPercentage(count, overview.totalPublications) + '%',
                  }"
                ></div>
              </div>
              <div class="w-10 text-sm text-slate-700 text-right font-medium">
                {{ count }}
              </div>
            </div>
          </div>
        </div>

        <!-- By Area -->
        <div class="bg-white rounded-lg shadow p-6">
          <h2 class="text-lg font-semibold text-slate-800 mb-4">
            Publications by Scientific Area
          </h2>
          <div
            v-if="Object.keys(overview.publicationsByArea || {}).length === 0"
            class="text-slate-400 text-sm"
          >
            No data available
          </div>
          <div v-else class="space-y-3">
            <div
              v-for="(count, area) in overview.publicationsByArea"
              :key="area"
              class="flex items-center gap-3"
            >
              <div class="w-36 text-sm text-slate-600 truncate" :title="area">
                {{ area }}
              </div>
              <div class="flex-1 bg-gray-100 rounded-full h-4 overflow-hidden">
                <div
                  class="h-full bg-green-500 rounded-full transition-all duration-500"
                  :style="{
                    width:
                      getPercentage(count, overview.totalPublications) + '%',
                  }"
                ></div>
              </div>
              <div class="w-10 text-sm text-slate-700 text-right font-medium">
                {{ count }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Recent Activity -->
      <div
        v-if="overview.recentActivity"
        class="bg-white rounded-lg shadow p-6"
      >
        <h2 class="text-lg font-semibold text-slate-800 mb-4">
          Recent Activity
        </h2>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <h3 class="text-sm font-medium text-slate-500 mb-2">Last 7 Days</h3>
            <div class="grid grid-cols-3 gap-4">
              <div class="text-center p-3 bg-blue-50 rounded">
                <div class="text-xl font-bold text-blue-600">
                  {{ overview.recentActivity?.last7Days?.newPublications || 0 }}
                </div>
                <div class="text-xs text-slate-500">Publications</div>
              </div>
              <div class="text-center p-3 bg-purple-50 rounded">
                <div class="text-xl font-bold text-purple-600">
                  {{ overview.recentActivity?.last7Days?.newComments || 0 }}
                </div>
                <div class="text-xs text-slate-500">Comments</div>
              </div>
              <div class="text-center p-3 bg-yellow-50 rounded">
                <div class="text-xl font-bold text-yellow-600">
                  {{ overview.recentActivity?.last7Days?.newRatings || 0 }}
                </div>
                <div class="text-xs text-slate-500">Ratings</div>
              </div>
            </div>
          </div>
          <div>
            <h3 class="text-sm font-medium text-slate-500 mb-2">
              Last 30 Days
            </h3>
            <div class="grid grid-cols-3 gap-4">
              <div class="text-center p-3 bg-blue-50 rounded">
                <div class="text-xl font-bold text-blue-600">
                  {{
                    overview.recentActivity?.last30Days?.newPublications || 0
                  }}
                </div>
                <div class="text-xs text-slate-500">Publications</div>
              </div>
              <div class="text-center p-3 bg-purple-50 rounded">
                <div class="text-xl font-bold text-purple-600">
                  {{ overview.recentActivity?.last30Days?.newComments || 0 }}
                </div>
                <div class="text-xs text-slate-500">Comments</div>
              </div>
              <div class="text-center p-3 bg-yellow-50 rounded">
                <div class="text-xl font-bold text-yellow-600">
                  {{ overview.recentActivity?.last30Days?.newRatings || 0 }}
                </div>
                <div class="text-xs text-slate-500">Ratings</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Personal Stats (only for authenticated users) -->
      <div
        v-if="auth.token.value && personal"
        class="bg-white rounded-lg shadow p-6"
      >
        <h2 class="text-lg font-semibold text-slate-800 mb-4">
          📊 Your Statistics
        </h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div
            class="text-center p-4 bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg"
          >
            <div class="text-2xl font-bold text-blue-700">
              {{ personal.publicationsUploaded || 0 }}
            </div>
            <div class="text-xs text-slate-600 mt-1">Publications Uploaded</div>
          </div>
          <div
            class="text-center p-4 bg-gradient-to-br from-purple-50 to-purple-100 rounded-lg"
          >
            <div class="text-2xl font-bold text-purple-700">
              {{ personal.commentsCreated || 0 }}
            </div>
            <div class="text-xs text-slate-600 mt-1">Comments Created</div>
          </div>
          <div
            class="text-center p-4 bg-gradient-to-br from-yellow-50 to-yellow-100 rounded-lg"
          >
            <div class="text-2xl font-bold text-yellow-700">
              {{ personal.ratingsGiven || 0 }}
            </div>
            <div class="text-xs text-slate-600 mt-1">Ratings Given</div>
          </div>
          <div
            class="text-center p-4 bg-gradient-to-br from-green-50 to-green-100 rounded-lg"
          >
            <div class="text-2xl font-bold text-green-700">
              {{ personal.tagsSubscribed || 0 }}
            </div>
            <div class="text-xs text-slate-600 mt-1">Tags Subscribed</div>
          </div>
        </div>

        <div
          v-if="personal.averageRatingReceived"
          class="mt-4 p-4 bg-slate-50 rounded-lg"
        >
          <div class="flex items-center justify-between">
            <div>
              <span class="text-sm text-slate-600"
                >Average rating received on your publications:</span
              >
              <span class="ml-2 text-xl font-bold text-yellow-600"
                >⭐
                {{ personal.averageRatingReceived?.toFixed(2) || "—" }}</span
              >
            </div>
            <div class="text-sm text-slate-500">
              ({{ personal.totalRatingsReceived || 0 }} total ratings)
            </div>
          </div>
        </div>
      </div>

      <!-- Top Publications -->
      <div class="bg-white rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-slate-800 mb-4">
          🏆 Top Publications
        </h2>
        <div v-if="topPublications.length === 0" class="text-slate-400 text-sm">
          No publications yet
        </div>
        <div v-else class="space-y-3">
          <div
            v-for="(pub, index) in topPublications"
            :key="pub.id"
            class="flex items-center gap-4 p-3 bg-slate-50 rounded-lg hover:bg-slate-100 transition-colors"
          >
            <div class="text-lg font-bold text-slate-400 w-6 text-center">
              {{ index + 1 }}
            </div>
            <div class="flex-1 min-w-0">
              <NuxtLink
                :to="`/publications/${pub.id}`"
                class="text-slate-800 font-medium hover:text-blue-600 truncate block"
              >
                {{ pub.title }}
              </NuxtLink>
              <div class="text-xs text-slate-500">
                by {{ pub.uploadedBy?.name || "Unknown" }}
              </div>
            </div>
            <div class="flex items-center gap-4 text-sm">
              <span class="text-yellow-600" title="Average Rating"
                >⭐ {{ pub.averageRating?.toFixed(1) || "—" }}</span
              >
              <span class="text-purple-600" title="Comments"
                >💬 {{ pub.commentsCount || 0 }}</span
              >
              <span class="text-slate-500" title="Views"
                >👁 {{ pub.viewsCount || 0 }}</span
              >
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="error" class="mt-4 p-4 bg-red-50 text-red-600 rounded-lg">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
// No auth middleware - statistics are public

const api = useApi();
const auth = useAuth();

const loading = ref(true);
const error = ref(null);
const overview = ref({});
const personal = ref(null);
const topPublications = ref([]);

const typeLabels = {
  ARTICLE: "Scientific Article",
  CONFERENCE: "Conference",
  BOOK_CHAPTER: "Book Chapter",
  BOOK: "Scientific Book",
  TECHNICAL_REPORT: "Technical Report",
  PATENT: "Patent",
  DATASET: "Dataset",
  SOFTWARE: "Software",
  AI_MODEL: "AI Model",
  DATABASE: "Database",
  THESIS_MASTER: "Master Thesis",
  THESIS_PHD: "PhD Thesis",
  OUTREACH: "Outreach Article",
};

const formatTypeName = (type) => {
  return typeLabels[type] || type.replace(/_/g, " ");
};

const getPercentage = (value, total) => {
  if (!total || total === 0) return 0;
  return Math.min(100, Math.round((value / total) * 100));
};

const fetchStats = async () => {
  loading.value = true;
  error.value = null;

  try {
    // Fetch overview stats
    const overviewResp = await api.get("/statistics/overview");
    overview.value = overviewResp.data || {};

    // Fetch personal stats if authenticated
    if (auth.token.value) {
      try {
        const personalResp = await api.get("/statistics/personal");
        personal.value = personalResp.data || null;
      } catch (e) {
        console.warn("Could not fetch personal stats", e);
      }
    }

    // Fetch top publications
    try {
      const topResp = await api.get("/statistics/top-publications", {
        params: { limit: 5, criteria: "rating" },
      });
      topPublications.value = topResp.data || [];
    } catch (e) {
      console.warn("Could not fetch top publications", e);
    }
  } catch (e) {
    console.error("Failed to fetch statistics", e);
    error.value = "Failed to load statistics. Please try again later.";
  } finally {
    loading.value = false;
  }
};

onMounted(fetchStats);
</script>
