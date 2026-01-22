<template>
  <div class="container mx-auto px-4 py-6">
    <div class="bg-white shadow rounded-lg p-6">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold text-slate-800">Activity Reports</h1>
        <div class="flex gap-2">
          <button
            @click="exportReport('csv')"
            :disabled="loading"
            class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
          >
            Export CSV
          </button>
          <button
            @click="exportReport('pdf')"
            :disabled="loading"
            class="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
          >
            Export PDF
          </button>
          <button
            @click="exportReport('json')"
            :disabled="loading"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
          >
            Export JSON
          </button>
        </div>
      </div>

      <!-- Filters -->
      <div class="bg-gray-50 p-4 rounded-lg mb-6">
        <h3 class="font-medium text-slate-700 mb-3">Filters</h3>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label class="block text-sm text-slate-600 mb-1">User IDs (comma separated)</label>
            <input
              v-model="filters.userIds"
              placeholder="e.g., 1,2,3"
              class="w-full border rounded px-3 py-2"
            />
          </div>
          <div>
            <label class="block text-sm text-slate-600 mb-1">Date From</label>
            <input
              v-model="filters.dateFrom"
              type="date"
              class="w-full border rounded px-3 py-2"
            />
          </div>
          <div>
            <label class="block text-sm text-slate-600 mb-1">Date To</label>
            <input
              v-model="filters.dateTo"
              type="date"
              class="w-full border rounded px-3 py-2"
            />
          </div>
          <div class="flex items-end">
            <button
              @click="loadReport"
              :disabled="loading"
              class="w-full px-4 py-2 bg-slate-600 text-white rounded hover:bg-slate-700 disabled:opacity-50"
            >
              {{ loading ? "Loading..." : "Apply Filters" }}
            </button>
          </div>
        </div>
      </div>

      <!-- Report Table -->
      <div v-if="loading" class="text-center py-8 text-slate-600">
        Loading activity data...
      </div>
      <div v-else-if="activities.length === 0" class="text-center py-8 text-slate-500">
        No activity records found. Adjust filters and try again.
      </div>
      <div v-else class="overflow-x-auto">
        <table class="w-full border-collapse">
          <thead>
            <tr class="bg-slate-100">
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">ID</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">User ID</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">Action</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">Entity Type</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">Entity ID</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">Description</th>
              <th class="border px-3 py-2 text-left text-sm font-medium text-slate-700">Timestamp</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="a in activities"
              :key="a.id"
              class="hover:bg-gray-50"
            >
              <td class="border px-3 py-2 text-sm">{{ a.id }}</td>
              <td class="border px-3 py-2 text-sm">{{ a.userId }}</td>
              <td class="border px-3 py-2 text-sm">
                <span class="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs">
                  {{ a.action }}
                </span>
              </td>
              <td class="border px-3 py-2 text-sm">{{ a.entityType }}</td>
              <td class="border px-3 py-2 text-sm">{{ a.entityId }}</td>
              <td class="border px-3 py-2 text-sm max-w-xs truncate" :title="a.description">
                {{ a.description }}
              </td>
              <td class="border px-3 py-2 text-sm">
                {{ a.timestamp ? new Date(a.timestamp).toLocaleString() : "-" }}
              </td>
            </tr>
          </tbody>
        </table>
        <div class="mt-4 text-sm text-slate-600">
          Total records: {{ activities.length }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

definePageMeta({
  middleware: ["auth"],
  layout: "default",
});

const api = useApi();
const auth = useAuth();

const loading = ref(false);
const activities = ref([]);
const filters = ref({
  userIds: "",
  dateFrom: "",
  dateTo: "",
});

// Check if user is admin
onMounted(() => {
  if (!auth.user.value || auth.user.value.role !== "ADMINISTRADOR") {
    navigateTo("/");
    return;
  }
  loadReport();
});

const buildQueryParams = () => {
  const params = new URLSearchParams();
  if (filters.value.userIds) {
    params.append("userIds", filters.value.userIds);
  }
  if (filters.value.dateFrom) {
    params.append("dateFrom", new Date(filters.value.dateFrom).toISOString());
  }
  if (filters.value.dateTo) {
    params.append("dateTo", new Date(filters.value.dateTo).toISOString());
  }
  return params.toString();
};

const loadReport = async () => {
  loading.value = true;
  try {
    const queryString = buildQueryParams();
    const url = `/reports/activity${queryString ? "?" + queryString : ""}`;
    const resp = await api.get(url + (queryString ? "&" : "?") + "format=json");
    activities.value = resp.data || [];
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error loading activity report");
  } finally {
    loading.value = false;
  }
};

const exportReport = async (format) => {
  try {
    const queryString = buildQueryParams();
    const url = `/reports/activity${queryString ? "?" + queryString + "&" : "?"}format=${format}`;
    
    const resp = await api.get(url, { responseType: "blob" });
    
    // Create blob and download
    const blob = new Blob([resp.data], {
      type: format === 'csv' ? 'text/csv' : format === 'pdf' ? 'application/pdf' : 'application/json'
    });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `activity-report.${format}`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  } catch (e) {
    console.error(e);
    alert(e?.response?.data?.message || "Error exporting report");
  }
};
</script>
