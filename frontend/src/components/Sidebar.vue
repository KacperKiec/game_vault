<script setup lang="ts">
import {GameListResponse, GameResponse, ListType} from "@/types/types";
import {onMounted, ref} from "vue";
import {storeToRefs} from "pinia";
import {useAuthStore} from "@/store/auth";
import {gameListService} from "@/service/game-list-service";

const emit = defineEmits(['setGames']);

  const setGames = (games: GameResponse[]) => {
    emit('setGames', games);
  };

  const authStore = useAuthStore();
  const activeList = ref(ListType.NONE)
  const { isAuthenticated } = storeToRefs(authStore);

  const platforms = [];
  const genres = [];
  const lists = ref<GameListResponse>({ wishlist: [], gamesToPlay: [], completedGames: [] });

  onMounted(async () => {
    if (isAuthenticated.value) {
      try {
        lists.value = await gameListService.getUserLists();
      } catch (err) {
        console.error("Failed to fetch lists:", err);
      }
    }
  });

  const setList = (type: ListType) => {
    let games: GameResponse[] = [];
    switch (type) {
      case ListType.WISHLIST: games = lists.value.wishlist; break;
      case ListType.TODO: games = lists.value.gamesToPlay; break;
      case ListType.COMPLETED: games = lists.value.completedGames; break;
      default: break;
    }
    setGames(games);
  }
</script>

<template>
  <aside class="w-64 bg-base-200 p-4 rounded-lg space-y-4">
    <!-- Search -->
    <div class="form-control">
      <label class="input input-bordered flex items-center gap-2 bg-base-300">
        <svg class="w-4 h-4 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
<!--        <input type="text" v-model="search" class="grow bg-transparent" placeholder="Search games..." />-->
      </label>
    </div>

    <div class="menu bg-base-300 rounded-box w-full">
      <ul v-if="isAuthenticated">
        <li class="menu-title">My Lists</li>
        <li><a :class="{ 'active': activeList === ListType.NONE }" @click="setList(ListType.NONE)">📂 All Games</a></li>
        <li><a :class="{ 'active': activeList === ListType.WISHLIST }" @click="setList(ListType.WISHLIST)">⭐ Wishlist <span class="badge badge-sm">{{ lists.wishlist.length }}</span></a></li>
        <li><a :class="{ 'active': activeList === ListType.TODO }" @click="setList(ListType.TODO)">🎯 To Play <span class="badge badge-sm">{{ lists.gamesToPlay.length }}</span></a></li>
        <li><a :class="{ 'active': activeList === ListType.COMPLETED }" @click="setList(ListType.COMPLETED)">✅ Played <span class="badge badge-sm">{{ lists.completedGames.length }}</span></a></li>
      </ul>
      <p v-else>Log in to see your game lists</p>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" checked />
      <div class="collapse-title font-medium">Platform</div>
      <div class="collapse-content">
        <label v-for="platform in platforms" class="label cursor-pointer">
          <span class="label-text">{{ platform.name }}</span>
          <input type="checkbox" class="checkbox checkbox-primary checkbox-sm" />
        </label>
      </div>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" />
      <div class="collapse-title font-medium">Genre</div>
      <div class="collapse-content">
        <label v-for="genre in genres" class="label cursor-pointer">
          <span class="label-text">{{ genre.name }}</span>
          <input type="checkbox" class="checkbox checkbox-primary checkbox-sm" />
        </label>
      </div>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" />
      <div class="collapse-title font-medium">Release Year</div>
      <div class="collapse-content">
        <input type="range" min="1980" max="2026" class="range range-primary range-sm" />
        <div class="flex justify-between text-xs text-base-content/60">
          <span>1980</span>
          <span>2026</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<style scoped>

</style>