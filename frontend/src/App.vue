<script setup lang="ts">

import Sidebar from "@/components/Sidebar.vue";
import Topbar from "@/components/Topbar.vue";
import AuthDialog from "@/components/auth/AuthDialog.vue";
import { ref } from 'vue';

const isAuthOpen = ref(false);


</script>

<template>
  <div class="min-h-screen bg-base-100 p-4">
    <Topbar @openAuthDialog="isAuthOpen = true"/>

    <div class="flex gap-6">
      <Sidebar class="md:flex flex-col" />

      <main class="flex-1">
        <router-view />
      </main>
    </div>
  </div>
  <AuthDialog v-if="isAuthOpen" @close="isAuthOpen = false"/>
</template>

<style scoped>

</style>
