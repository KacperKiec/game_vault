<script setup lang="ts">

import {Game, GameListRequest} from "@/types/types";
  import GameCard from "@/components/games/GameCard.vue";

  const props = defineProps<{
    games: Game[],
    totalPages: number,
    currentPage: number,
  }>();

  const emit = defineEmits(['setNewPage', 'setNewListItem']);

  const setNewListItem = (game: Game) => {
    emit('setNewListItem', game);
  }
  const setNewPage = (page: number) => {
    emit('setNewPage', page);
  }

  const handleNewListItem = (game: Game) => {
    setNewListItem(game);
  }

</script>

<template>
  <TransitionGroup tag="div" name="fade-slide" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
    <GameCard v-for="(game, index) in games" :key="game.guid" :game="game" :style="{ transitionDelay: `${index * 50}ms` }" @set-new-list-item="handleNewListItem"/>
  </TransitionGroup>

  <div v-show="games.length > 0" class="flex justify-center mt-8 mb-4">
    <div class="join">
      <button
          class="join-item btn"
          :disabled="currentPage <= 1"
          @click="setNewPage(currentPage - 1)"
      >
        «
      </button>

      <label class="join-item btn">Page {{ currentPage }} of {{ totalPages }}</label>

      <button
          class="join-item btn"
          :disabled="currentPage >= totalPages"
          @click="setNewPage(currentPage + 1)"
      >
        »
      </button>
    </div>
  </div>
</template>

<style scoped>
  .fade-slide-enter-from {
    opacity: 0;
    transform: translateY(30px);
  }

  .fade-slide-enter-active {
    transition: all 0.5s ease-out;
  }

  .fade-slide-enter-to {
    opacity: 1;
    transform: translateY(0);
  }

  .fade-slide-leave-active {
    transition: all 0.3s ease-in;
    position: absolute;
  }

  .fade-slide-leave-to {
    opacity: 0;
    transform: scale(0.9);
  }

  .fade-slide-move {
    transition: transform 0.4s ease;
  }
</style>