<script setup lang="ts">

</script>

<!-- GameDetail.vue -->
<template>
  <div class="min-h-screen">
    <!-- Hero Section with Background -->
    <div class="relative h-96" :style="{ backgroundImage: `url(${game.background_image})` }">
      <div class="absolute inset-0 bg-gradient-to-t from-base-100 via-base-100/60 to-transparent" />

      <div class="absolute bottom-0 left-0 right-0 p-8 flex gap-6">
        <!-- Cover -->
        <img :src="game.background_image" class="w-48 h-64 object-cover rounded-lg shadow-2xl" />

        <div class="flex-1">
          <h1 class="text-4xl font-bold mb-2">{{ game.name }}</h1>

          <div class="flex items-center gap-4 mb-4">
            <div class="badge badge-accent badge-lg">{{ game.metacritic }}</div>
            <span class="text-base-content/60">{{ game.released }}</span>
            <span>{{ game.playtime }}h avg playtime</span>
          </div>

          <!-- Action Buttons -->
          <div class="flex gap-2">
            <div class="dropdown">
              <label tabindex="0" class="btn btn-primary">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Add to List
              </label>
              <ul tabindex="0" class="dropdown-content menu bg-base-300 rounded-box w-52 p-2 shadow-lg mt-2">
                <li><a>⭐ Wishlist</a></li>
                <li><a>🎯 To Play</a></li>
                <li><a>✅ Played</a></li>
              </ul>
            </div>
            <button class="btn btn-outline">Visit Website</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="container mx-auto p-8 grid grid-cols-3 gap-8">
      <!-- Main Info -->
      <div class="col-span-2 space-y-6">
        <div class="prose prose-invert max-w-none">
          <h2>About</h2>
          <p v-html="game.description"></p>
        </div>

        <!-- Screenshots Gallery -->
        <div>
          <h2 class="text-xl font-bold mb-4">Screenshots</h2>
          <div class="grid grid-cols-2 gap-4">
            <img v-for="screenshot in game.screenshots"
                 :src="screenshot.image"
                 class="rounded-lg hover:scale-105 transition-transform cursor-pointer" />
          </div>
        </div>
      </div>

      <!-- Sidebar Info -->
      <div class="space-y-4">
        <div class="bg-base-200 rounded-lg p-4">
          <h3 class="font-bold mb-3">Game Info</h3>
          <dl class="space-y-2 text-sm">
            <div class="flex justify-between">
              <dt class="text-base-content/60">Platforms</dt>
              <dd>{{ game.platforms?.map(p => p.platform.name).join(', ') }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-base-content/60">Genres</dt>
              <dd>{{ game.genres?.map(g => g.name).join(', ') }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-base-content/60">Developer</dt>
              <dd>{{ game.developers?.[0]?.name }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-base-content/60">Publisher</dt>
              <dd>{{ game.publishers?.[0]?.name }}</dd>
            </div>
          </dl>
        </div>

        <!-- Stores -->
        <div class="bg-base-200 rounded-lg p-4">
          <h3 class="font-bold mb-3">Where to Buy</h3>
          <div class="flex flex-wrap gap-2">
            <a v-for="store in game.stores" class="btn btn-sm btn-ghost">
              {{ store.store.name }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>