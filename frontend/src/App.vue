<script setup lang="ts">

import Sidebar from "@/components/Sidebar.vue";
import Topbar from "@/components/Topbar.vue";
</script>

<template>
  <div class="min-h-screen bg-base-100 p-4">
    <Topbar />

    <div class="flex gap-6">
      <Sidebar class="md:flex flex-col" />

      <main class="flex-1">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>

</style>
