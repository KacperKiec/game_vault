<script setup lang="ts">

import {Game, GameListRequest, ListType} from "@/types/types";
import {computed} from "vue";
import {gameListService} from "@/service/game-list-service";

const props = defineProps<{
    game: Game
  }>();

  const emit = defineEmits(['setNewListItem']);

  const setNewListItem = (game: Game) => {
    emit('setNewListItem', game);
  }

  const CATEGORIES = {
    PLAYSTATION: { label: 'PlayStation', icon: '🟦', color: 'bg-blue-600/20 text-blue-400' },
    XBOX: { label: 'Xbox', icon: '🟩', color: 'bg-green-600/20 text-green-400' },
    PC: { label: 'PC', icon: '🖥️', color: 'bg-slate-600/20 text-slate-400' },
    NINTENDO: { label: 'Nintendo', icon: '🟥', color: 'bg-red-600/20 text-red-400' },
    MOBILE: { label: 'Mobile', icon: '📱', color: 'bg-yellow-600/20 text-yellow-400' },
    RETRO: { label: 'Retro', icon: '🕹️', color: 'bg-purple-600/20 text-purple-400' }
  };

  const gameCategories = computed(() => {
    if (!props.game.platforms) return [];

    const uniqueCategories = new Set<keyof typeof CATEGORIES>();

    props.game.platforms.forEach(platform => {
      const name = platform.toLowerCase();

      if (name.includes('playstation') || name.includes('ps ') || name.includes('psp') || name.includes('vita')) {
        uniqueCategories.add('PLAYSTATION');
      } else if (name.includes('xbox')) {
        uniqueCategories.add('XBOX');
      } else if (name.includes('pc') || name.includes('windows') || name.includes('linux') || name.includes('macos') || name.includes('macintosh')) {
        uniqueCategories.add('PC');
      } else if (name.includes('nintendo') || name.includes('switch') || name.includes('wii') || name.includes('nes') || name.includes('snes') || name.includes('game boy') || name.includes('gamecube')) {
        uniqueCategories.add('NINTENDO');
      } else if (name.includes('ios') || name.includes('android')) {
        uniqueCategories.add('MOBILE');
      } else {
        uniqueCategories.add('RETRO');
      }
    });

    return Array.from(uniqueCategories).map(key => CATEGORIES[key]);
  });

  const handleAddingToList = async (guid: number, type: ListType) => {
    props.game.listType = type;
    const dto: GameListRequest = {
      guid: guid,
      listType: type,
    }
    const success = await gameListService.modifyLists(dto);
    if (success) setNewListItem(props.game);
  }

</script>

<template>
  <div class="card bg-base-200 shadow-xl hover:shadow-2xl hover:scale-[1.02] transition-all duration-300 group">
    <figure class="relative overflow-visible">
      <img :src="game.backgroundImage" :alt="game.name" class="h-48 w-full object-cover" />

      <div class="absolute inset-0 bg-linear-to-t from-base-100/90 to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex items-end p-4">
        <div class="dropdown dropdown-top">
          <label tabindex="0" class="btn btn-primary btn-sm">+ Add to List</label>
          <ul tabindex="0" class="dropdown-content menu bg-base-300 rounded-box w-42 p-2 shadow-lg">
            <li><a @click="handleAddingToList(game.guid, ListType.WISHLIST)">⭐ Wishlist <span v-if="game.listType === ListType.WISHLIST" class="badge badge-sm">✔️</span></a></li>
            <li><a @click="handleAddingToList(game.guid, ListType.TODO)">🎯 To Play <span v-if="game.listType === ListType.TODO" class="badge badge-sm">✔️</span></a></li>
            <li><a @click="handleAddingToList(game.guid, ListType.COMPLETED)">✅ Played <span v-if="game.listType === ListType.COMPLETED" class="badge badge-sm">✔️</span></a></li>
          </ul>
        </div>
      </div>

      <div class="absolute top-2 right-2 flex gap-1.5 transition-all">
        <div v-for="category in gameCategories"
             :key="category.label"
             class="tooltip tooltip-bottom"
             :data-tip="category.label">
          <span :class="['badge badge-sm border-none shadow-sm py-3 px-2 font-bold', category.color]">
            {{ category.icon }}
          </span>
        </div>
      </div>
    </figure>

    <div class="card-body p-4">
      <div class="tooltip tooltip-bottom before:max-w-xs" :data-tip="game.name">
        <router-link
            :to="{ name: 'game-details', params: { id: game.guid } }"
            class="card-title text-base line-clamp-1 hover:text-primary transition-colors cursor-pointer"
        >
          {{ game.name }}
        </router-link>
      </div>

      <div class="flex items-center gap-2 text-sm text-base-content/60">
        <span>{{ new Date(game.releaseDate).toLocaleDateString() }}</span>
      </div>

      <div class="flex flex-wrap gap-1 mt-2">
        <span v-for="genre in game.genres?.slice(0, 2)"
              class="badge badge-ghost badge-xs">
          {{ genre }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>