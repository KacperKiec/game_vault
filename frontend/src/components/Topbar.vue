<script setup lang="ts">
import { useAuthStore } from '@/store/auth';
import { storeToRefs } from 'pinia';
import NotificationPanel from "@/components/NotificationPanel.vue";

const authStore = useAuthStore();
const { isAuthenticated, username } = storeToRefs(authStore);

const emit = defineEmits(['openAuthDialog', 'logout']);

const openAuthDialog = () => {
  emit('openAuthDialog');
};

const logout = () => {
  emit('logout');
}

const handleLogout = () => {
  authStore.logout();
  emit('logout');
}

</script>

<template>
  <div class="navbar bg-base-200 border-b border-base-300 px-4 mb-6 shadow-sm rounded-lg">
    <div class="flex-1">
      <RouterLink to="/" class="text-2xl font-black tracking-tighter text-primary uppercase">
        Game Vault
      </RouterLink>
    </div>

    <div class="flex-none gap-2">

      <div v-if="!isAuthenticated">
        <button class="btn btn-primary btn-sm rounded-md px-6" @click="openAuthDialog">
          Log In
        </button>
      </div>

      <div v-else>
        <NotificationPanel/>
        <div class="dropdown dropdown-end">
          <div tabindex="0" role="button" class="btn btn-ghost btn-sm rounded-md border border-base-300">
            <div class="avatar placeholder mr-2">
              <div class="bg-neutral text-neutral-content rounded-full w-6">
                <span class="text-xs flex justify-center mt-1">{{ username?.charAt(0).toUpperCase() }}</span>
              </div>
            </div>
            {{ username }}
          </div>
          <ul tabindex="0" class="mt-3 z-1 p-2 shadow menu menu-sm dropdown-content bg-base-300 rounded-box w-52">
            <li><a @click="handleLogout" class="text-error">Logout</a></li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>