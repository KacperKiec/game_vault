<script setup lang="ts">
import { useAuthStore } from '@/store/auth';
import { storeToRefs } from 'pinia';

const authStore = useAuthStore();
const { isAuthenticated, username } = storeToRefs(authStore);
</script>

<template>
  <div class="navbar bg-base-200 border-b border-base-300 px-4 mb-6 shadow-sm rounded-lg">
    <div class="flex-1">
      <RouterLink to="/" class="text-2xl font-black tracking-tighter text-primary uppercase">
        Game Vault
      </RouterLink>
    </div>

    <div class="flex-none gap-2">
      <RouterLink
          v-if="isAuthenticated"
          to="/lists"
          class="btn btn-ghost btn-sm rounded-md font-medium"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 10h16M4 14h16M4 18h16" />
        </svg>
        My Lists
      </RouterLink>

      <div v-if="!isAuthenticated">
        <RouterLink to="/login" class="btn btn-primary btn-sm rounded-md px-6">
          Log In
        </RouterLink>
      </div>

      <div v-else class="dropdown dropdown-end">
        <div tabindex="0" role="button" class="btn btn-ghost btn-sm rounded-md border border-base-300">
          <div class="avatar placeholder mr-2">
            <div class="bg-neutral text-neutral-content rounded-full w-6">
              <span class="text-xs">{{ username?.charAt(0).toUpperCase() }}</span>
            </div>
          </div>
          {{ username }}
        </div>
        <ul tabindex="0" class="mt-3 z-1 p-2 shadow menu menu-sm dropdown-content bg-base-300 rounded-box w-52">
          <li><RouterLink to="/profile">Profile Settings</RouterLink></li>
          <li><a @click="authStore.logout()" class="text-error">Logout</a></li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>