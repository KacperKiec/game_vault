<script setup lang="ts">

</script>

<template>
  <div class="card bg-base-200 shadow-xl hover:shadow-2xl hover:scale-[1.02] transition-all duration-300 group">
    <!-- Cover Image -->
    <figure class="relative overflow-hidden">
      <img :src="game.background_image" :alt="game.name" class="h-48 w-full object-cover" />

      <!-- Hover Overlay -->
      <div class="absolute inset-0 bg-gradient-to-t from-base-100/90 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-4">
        <div class="dropdown dropdown-top">
          <label tabindex="0" class="btn btn-primary btn-sm">+ Add to List</label>
          <ul tabindex="0" class="dropdown-content menu bg-base-300 rounded-box w-52 p-2 shadow-lg">
            <li><a>⭐ Wishlist</a></li>
            <li><a>🎯 To Play</a></li>
            <li><a>✅ Played</a></li>
            <li><a>💾 Backlog</a></li>
          </ul>
        </div>
      </div>

      <!-- Platform badges -->
      <div class="absolute top-2 right-2 flex gap-1">
        <span v-for="platform in game.platforms?.slice(0, 3)"
              class="badge badge-sm bg-base-100/80">
          {{ getPlatformIcon(platform) }}
        </span>
      </div>
    </figure>

    <div class="card-body p-4">
      <h3 class="card-title text-base line-clamp-1">{{ game.name }}</h3>

      <div class="flex items-center gap-2 text-sm text-base-content/60">
        <span>{{ game.released?.slice(0, 4) }}</span>
        <span class="badge badge-accent badge-sm">{{ game.metacritic || 'N/A' }}</span>
      </div>

      <!-- Genres -->
      <div class="flex flex-wrap gap-1 mt-2">
        <span v-for="genre in game.genres?.slice(0, 2)"
              class="badge badge-ghost badge-xs">
          {{ genre.name }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>