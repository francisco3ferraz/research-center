<template>
  <div class="container mx-auto px-4 py-8">
    <!-- Header Card -->
    <div class="bg-white shadow rounded-lg overflow-hidden mb-6">
      <div class="p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div
              class="w-16 h-16 rounded-full bg-blue-100 flex items-center justify-center text-2xl font-semibold text-blue-700"
            >
              {{
                auth.user.value?.name
                  ? auth.user.value.name
                      .split(" ")
                      .map((n) => n[0])
                      .join("")
                      .slice(0, 2)
                  : "U"
              }}
            </div>
            <div>
              <div class="text-xl font-bold text-slate-800">
                {{
                  auth.user.value?.name ||
                  auth.user.value?.username ||
                  "Utilizador"
                }}
              </div>
              <div class="text-sm text-slate-500">
                {{ auth.user.value?.email }}
              </div>
            </div>
          </div>
          <NuxtLink to="/" class="text-sm text-slate-500 hover:underline"
            >Voltar</NuxtLink
          >
        </div>
      </div>
    </div>

    <!-- Sections Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Section 1: Personal Information -->
      <div class="bg-white shadow rounded-lg p-6">
        <h3
          class="text-lg font-bold text-slate-800 mb-4 border-b pb-2 flex items-center gap-2"
        >
          <span>👤</span> Informações Pessoais
        </h3>

        <div v-if="loading" class="text-slate-600">A carregar...</div>
        <form @submit.prevent="saveProfile" v-else class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700"
              >Nome Completo</label
            >
            <input
              v-model="form.name"
              class="w-full mt-1 border rounded px-3 py-2 focus:ring-2 focus:ring-blue-100 focus:border-blue-400 outline-none"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700"
              >Email</label
            >
            <input
              v-model="form.email"
              type="email"
              class="w-full mt-1 border rounded px-3 py-2 focus:ring-2 focus:ring-blue-100 focus:border-blue-400 outline-none"
            />
          </div>

          <div class="bg-slate-50 rounded p-4 mt-4 border border-slate-100">
            <h4
              class="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2"
            >
              Detalhes da Conta
            </h4>
            <div class="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span class="block text-slate-400 text-xs">Username</span>
                <span class="font-medium text-slate-700">{{
                  auth.user.value?.username
                }}</span>
              </div>
              <div>
                <span class="block text-slate-400 text-xs">Papel</span>
                <span
                  class="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800"
                >
                  {{ auth.user.value?.role }}
                </span>
              </div>
              <div>
                <span class="block text-slate-400 text-xs">Estado</span>
                <span
                  :class="
                    auth.user.value?.active ? 'text-green-600' : 'text-red-600'
                  "
                  class="font-medium"
                >
                  {{ auth.user.value?.active ? "Ativo" : "Inativo" }}
                </span>
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between pt-2">
            <div>
              <p v-if="profileMsg" class="text-sm text-green-600">
                {{ profileMsg }}
              </p>
              <p v-if="profileError" class="text-sm text-red-600">
                {{ profileError }}
              </p>
            </div>
            <div class="flex gap-2">
              <button
                type="button"
                @click="reload"
                class="px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100 rounded"
              >
                Cancelar
              </button>
              <button
                type="submit"
                class="bg-blue-600 text-white px-4 py-2 rounded shadow hover:bg-blue-700 text-sm font-medium"
              >
                Guardar Alterações
              </button>
            </div>
          </div>
        </form>
      </div>

      <div class="space-y-6">
        <!-- Section 2: Security -->
        <div class="bg-white shadow rounded-lg p-6">
          <h3
            class="text-lg font-bold text-slate-800 mb-4 border-b pb-2 flex items-center gap-2"
          >
            <span>🔒</span> Segurança
          </h3>
          <form @submit.prevent="changePassword" class="space-y-3">
            <div>
              <label class="block text-sm font-medium text-slate-700"
                >Senha Atual</label
              >
              <input
                v-model="pwd.oldPassword"
                type="password"
                class="w-full mt-1 border rounded px-3 py-2 focus:ring-2 focus:ring-blue-100 focus:border-blue-400 outline-none"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700"
                >Nova Senha</label
              >
              <input
                v-model="pwd.newPassword"
                type="password"
                class="w-full mt-1 border rounded px-3 py-2 focus:ring-2 focus:ring-blue-100 focus:border-blue-400 outline-none"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700"
                >Confirmar Nova Senha</label
              >
              <input
                v-model="pwd.confirmPassword"
                type="password"
                class="w-full mt-1 border rounded px-3 py-2 focus:ring-2 focus:ring-blue-100 focus:border-blue-400 outline-none"
              />
            </div>

            <div class="flex items-center justify-between pt-2">
              <div class="flex-1 mr-2">
                <p v-if="pwdMsg" class="text-xs text-green-600 leading-tight">
                  {{ pwdMsg }}
                </p>
                <p v-if="pwdError" class="text-xs text-red-600 leading-tight">
                  {{ pwdError }}
                </p>
              </div>
              <div class="flex gap-2">
                <button
                  type="button"
                  @click="clearPwd"
                  class="px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100 rounded"
                >
                  Limpar
                </button>
                <button
                  type="submit"
                  class="bg-green-600 text-white px-4 py-2 rounded shadow hover:bg-green-700 text-sm font-medium whitespace-nowrap"
                >
                  Alterar Senha
                </button>
              </div>
            </div>
          </form>
        </div>

        <!-- Section 3: Activity -->
        <div class="bg-white shadow rounded-lg p-6">
          <h3
            class="text-lg font-bold text-slate-800 mb-4 border-b pb-2 flex items-center gap-2"
          >
            <span>📊</span> Atividade
          </h3>
          <p class="text-sm text-slate-600 mb-4">
            Consulte o histórico de todas as suas ações na plataforma, incluindo
            publicações, edições, comentários e avaliações.
          </p>
          <NuxtLink
            to="/profile/activity"
            class="flex items-center justify-center gap-2 w-full border-2 border-dashed border-slate-300 rounded-lg p-4 text-slate-600 hover:border-blue-500 hover:text-blue-600 hover:bg-blue-50 transition-colors group"
          >
            <div class="bg-slate-100 p-2 rounded-full group-hover:bg-white">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <span class="font-medium">Ver Histórico de Atividade Completo</span>
          </NuxtLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

definePageMeta({
  middleware: 'auth'
})

const api = useApi();
const auth = useAuth();

const loading = ref(true);
const form = ref({ name: "", email: "" });
const profileMsg = ref("");
const profileError = ref("");

const pwd = ref({ oldPassword: "", newPassword: "", confirmPassword: "" });
const pwdMsg = ref("");
const pwdError = ref("");

const load = async () => {
  loading.value = true;
  try {
    const resp = await api.get("/auth/user");
    const u = resp.data;
    form.value.name = u.name || "";
    form.value.email = u.email || "";
    auth.user.value = u;
  } catch (e) {
    console.error(e);
    if (!auth.token.value) return navigateTo("/auth/login");
  } finally {
    loading.value = false;
  }
};

const saveProfile = async () => {
  profileMsg.value = "";
  profileError.value = "";
  try {
    const resp = await api.put("/users/profile", {
      name: form.value.name,
      email: form.value.email,
    });
    profileMsg.value = "Perfil atualizado com sucesso.";
    auth.user.value = resp.data;
  } catch (e) {
    profileError.value =
      e?.response?.data?.message || "Erro ao atualizar perfil.";
  }
};

const changePassword = async () => {
  pwdMsg.value = "";
  pwdError.value = "";
  if (pwd.value.newPassword !== pwd.value.confirmPassword) {
    pwdError.value = "Nova senha e confirmação não coincidem.";
    return;
  }
  try {
    await api.post("/auth/set-password", {
      oldPassword: pwd.value.oldPassword,
      newPassword: pwd.value.newPassword,
      confirmPassword: pwd.value.confirmPassword,
    });
    pwdMsg.value = "Senha alterada com sucesso.";
    clearPwd();
  } catch (e) {
    pwdError.value = e?.response?.data || "Erro ao alterar senha.";
  }
};

const clearPwd = () => {
  pwd.value.oldPassword = "";
  pwd.value.newPassword = "";
  pwd.value.confirmPassword = "";
  pwdMsg.value = "";
  pwdError.value = "";
};
const reload = () => load();

onMounted(load);
</script>
