<script setup lang="ts">

  import {Game} from "@/types/types";
  import GameCard from "@/components/games/GameCard.vue";

  defineProps<{
    games: Game[]
  }>();


</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
    <GameCard v-for="game in games" :key="game.guid" :game="game" />
  </div>
</template>

<style scoped>

</style>