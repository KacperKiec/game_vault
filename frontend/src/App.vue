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
  import { useUiStore } from '@/store/ui';

  const uiStore = useUiStore();
  const authStore = useAuthStore();
  const { isAuthenticated } = storeToRefs(authStore);
  const isAuthOpen = ref(false);
  const displayedGames = ref<Game[]>([]);
  const gameApiParams = ref<GameAPIParams>({ gameName: '', gameParams: { genres: [], platforms: [] }, gameDates: '' })
  const activeList = ref('');
  const pageNumber = ref(1);
  const totalPages = ref(0);
  const pageSize = 20;
  const lists = ref<GameListResponse>({ wishlist: [], gamesToPlay: [], completedGames: [] });

  const handleGameList = (listType: ListType) => {
    activeList.value = listType;
    switch (listType) {
      case ListType.WISHLIST: displayedGames.value = lists.value.wishlist; totalPages.value = Math.ceil(lists.value.wishlist.length / pageSize); break;
      case ListType.TODO: displayedGames.value = lists.value.gamesToPlay; totalPages.value = Math.ceil(lists.value.gamesToPlay.length / pageSize); break;
      case ListType.COMPLETED: displayedGames.value = lists.value.completedGames; totalPages.value = Math.ceil(lists.value.completedGames.length / pageSize); break;
      default: prepareGames(); break;
    }
  };

  const handleGameName = (name: string) => {
    gameApiParams.value.gameName = name;
    prepareGames();
  }

  const handleGameParams = (params: GameParams) => {
    gameApiParams.value.gameParams = params;
    prepareGames();
  }

  const handleGameDates = (dates: string) => {
    gameApiParams.value.gameDates = dates;
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
    lists.value.gamesToPlay = lists.value.gamesToPlay.filter(g => g.guid !== updatedGame.guid);
    lists.value.completedGames = lists.value.completedGames.filter(g => g.guid !== updatedGame.guid);

    const gameToPush = { ...updatedGame};
    switch (updatedGame.listType) {
      case ListType.WISHLIST: lists.value.wishlist.push(gameToPush); break;
      case ListType.TODO: lists.value.gamesToPlay.push(gameToPush); break;
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

  const prepareGames = async () => {
    try {
      const result = await gameService.getGames(pageNumber.value, gameApiParams.value);
      displayedGames.value = result.games;
      totalPages.value = result.totalPages;
    } catch (err) {
      console.error("Failed to fetch games:", err);
    }
  }
</script>

<template>
  <div class="min-h-screen bg-base-100 p-4">
    <Topbar @openAuthDialog="isAuthOpen = true"/>

    <div class="flex gap-6">
      <Sidebar class="md:flex flex-col"
               @set-game-list="handleGameList"
               @set-game-name="handleGameName"
               @set-game-params="handleGameParams"
               @set-dates="handleGameDates"
               :wishlistSize="lists.wishlist.length"
               :toPlaySize="lists.gamesToPlay.length"
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
