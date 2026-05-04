<script setup lang="ts">

import Sidebar from "@/components/Sidebar.vue";
import Topbar from "@/components/Topbar.vue";
import AuthDialog from "@/components/auth/AuthDialog.vue";
import {onMounted, ref} from 'vue';
import {Game, GameAPIParams, GameListResponse, GameParams, ListType} from "@/types/types";
import {storeToRefs} from "pinia";
import {gameListService} from "@/service/game-list-service";
import {useAuthStore} from "@/store/auth";
import {gameService} from "@/service/game-service";
import {useUiStore} from '@/store/ui';
import {useRouter} from "vue-router";

  const uiStore = useUiStore();
  const authStore = useAuthStore();
  const router = useRouter();
  const { isAuthenticated } = storeToRefs(authStore);
  const isAuthOpen = ref(false);
  const displayedGames = ref<Game[]>([]);
  const gameApiParams = ref<GameAPIParams>({ gameName: '', gameParams: { genres: [], platforms: [] }, gameDates: '' })
  const activeList = ref(ListType.NONE);
  const pageNumber = ref(1);
  const totalPages = ref(0);
  const pageSize = 20;
  const lists = ref<GameListResponse>({ wishlist: [], ownedGames: [], completedGames: [] });

  const handleGameList = (listType: ListType) => {
    activeList.value = listType;
    pageNumber.value = 1;
    prepareGames();
  };

  const handleGameName = (name: string) => {
    gameApiParams.value.gameName = name;
    pageNumber.value = 1;
    prepareGames();
  }

  const handleGameParams = (params: GameParams) => {
    gameApiParams.value.gameParams = params;
    pageNumber.value = 1;
    prepareGames();
  }

  const handleGameDates = (dates: string) => {
    gameApiParams.value.gameDates = dates;
    pageNumber.value = 1;
    prepareGames();
  }

  const handlePageChange = (newPage: number) => {
    pageNumber.value = newPage;
    prepareGames();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleNewListItem = (updatedGame: Game) => {
    const gameInDisplay = displayedGames.value.find(g => g.guid === updatedGame.guid);
    if (gameInDisplay) {
      gameInDisplay.listType = updatedGame.listType;
    }

    lists.value.wishlist = lists.value.wishlist.filter(g => g.guid !== updatedGame.guid);
    lists.value.ownedGames = lists.value.ownedGames.filter(g => g.guid !== updatedGame.guid);
    lists.value.completedGames = lists.value.completedGames.filter(g => g.guid !== updatedGame.guid);

    const gameToPush = { ...updatedGame};
    switch (updatedGame.listType) {
      case ListType.WISHLIST: lists.value.wishlist.push(gameToPush); break;
      case ListType.OWNED: lists.value.ownedGames.push(gameToPush); break;
      case ListType.COMPLETED: lists.value.completedGames.push(gameToPush); break;
      default: break;
    }
  }

  const handleLoginSuccess = async () => {
    await fetchUserLists();
  };

  const fetchUserLists = async () => {
    if (isAuthenticated.value) {
      try {
        lists.value = await gameListService.getUserLists();
      } catch (err) {
        console.error("Failed to fetch lists:", err);
      }
    }
  };

  onMounted(async () => {
    await prepareGames();
    await fetchUserLists();
  });

  const filterLocalGames = (games: Game[]) => {
    return games.filter(game => {
      const cleanGameName = game.name.replace(/"/g, '').toLowerCase();
      const searchName = gameApiParams.value.gameName.toLowerCase();

      const matchesName = !searchName || cleanGameName.includes(searchName);

      const genreMatch = gameApiParams.value.gameParams.genres.length === 0 ||
          game.genres.some(g => gameApiParams.value.gameParams.genres.some(sg => sg.name === g));

      const platformMatch = gameApiParams.value.gameParams.platforms.length === 0 ||
          game.platforms.some(p => gameApiParams.value.gameParams.platforms.some(sp => sp.name === p));

      let dateMatch = true;
      if (gameApiParams.value.gameDates && game.releaseDate) {
        const [start, end] = gameApiParams.value.gameDates.split(',').map(d => new Date(d).getFullYear());
        const gameYear = new Date(game.releaseDate).getFullYear();
        dateMatch = gameYear >= start && gameYear <= end;
      }

      return matchesName && genreMatch && platformMatch && dateMatch;
    });
  };

  const prepareGames = async () => {

    if (activeList.value === ListType.NONE) {
      try {
        const result = await gameService.getGames(pageNumber.value, gameApiParams.value);
        displayedGames.value = result.games;
        totalPages.value = result.totalPages;
      } catch (err) {
        console.error("Failed to fetch games:", err);
      }
    } else {
      let sourceList: Game[] = [];

      switch (activeList.value) {
        case ListType.WISHLIST: sourceList = lists.value.wishlist; break;
        case ListType.OWNED: sourceList = lists.value.ownedGames; break;
        case ListType.COMPLETED: sourceList = lists.value.completedGames; break;
      }

      const filtered = filterLocalGames(sourceList);

      totalPages.value = Math.ceil(filtered.length / pageSize);
      const start = (pageNumber.value - 1) * pageSize;
      displayedGames.value = filtered.slice(start, start + pageSize);
    }
  };

  const handleLogout = async () => {
    lists.value = {wishlist: [], ownedGames: [], completedGames: []};
    if (activeList.value !== ListType.NONE) {
      handleGameList(ListType.NONE);
    }
    await router.push('/');
  }
</script>

<template>
  <div class="min-h-screen bg-base-100 p-4">
    <Topbar
        @openAuthDialog="isAuthOpen = true"
        @logout="handleLogout"/>

    <div class="flex gap-6">
      <Sidebar class="md:flex flex-col"
               @set-game-list="handleGameList"
               @set-game-name="handleGameName"
               @set-game-params="handleGameParams"
               @set-dates="handleGameDates"
               :wishlistSize="lists.wishlist.length"
               :ownedSize="lists.ownedGames.length"
               :completedSize="lists.completedGames.length"
      />

      <main class="flex-1">
        <router-view
            @set-new-page="handlePageChange"
            @set-new-list-item="handleNewListItem"
            :games="displayedGames"
            :totalPages="totalPages"
            :currentPage="pageNumber"/>
      </main>
    </div>

    <Transition name="fade">
      <div v-if="uiStore.isLoading"
           class="fixed inset-0 z-100 flex items-center justify-center bg-base-100/60 backdrop-blur-sm">
        <div class="flex flex-col items-center gap-4">
          <span class="loading loading-dots loading-lg text-primary"></span>
          <p class="font-bold text-primary animate-pulse">Loading games...</p>
        </div>
      </div>
    </Transition>

  </div>
  <AuthDialog v-if="isAuthOpen" @close="isAuthOpen = false" @login-success="handleLoginSuccess"/>
</template>

<style scoped>
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s ease;
  }
  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }
</style>
