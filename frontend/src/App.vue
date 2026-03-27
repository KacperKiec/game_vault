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

  const authStore = useAuthStore();
  const { isAuthenticated } = storeToRefs(authStore);
  const isAuthOpen = ref(false);
  const displayedGames = ref<Game[]>([]);
  const gameApiParams = ref<GameAPIParams>({ gameName: '', gameParams: { genres: [], platforms: [] }, gameDates: '' })
  const activeList = ref('');
  const pageNumber = ref(0);
  const lists = ref<GameListResponse>({ wishlist: [], gamesToPlay: [], completedGames: [] });

  const handleGameList = (listType: ListType) => {
    activeList.value = listType;
    switch (listType) {
      case ListType.WISHLIST: displayedGames.value = lists.value.wishlist; break;
      case ListType.TODO: displayedGames.value = lists.value.gamesToPlay; break;
      case ListType.COMPLETED: displayedGames.value = lists.value.completedGames; break;
      default: prepareGames(); break;
    }
  };

  const handleGameName = (name: string) => {
    gameApiParams.value.gameName = name;
    prepareGames();
  }

  const handleGameParams = (params: GameParams) => {
    gameApiParams.value.gameParams = params;
    console.log(params);
    prepareGames();
  }

  const handleGameDates = (dates: string) => {
    gameApiParams.value.gameDates = dates;
    prepareGames();
  }

  onMounted(async () => {
    try {
      await prepareGames();
    } catch (err) {
      console.error("Failed to fetch games:", err);
    }

    if (isAuthenticated.value) {
      try {
        console.log("authenticated: " + isAuthenticated);
        lists.value = await gameListService.getUserLists();
      } catch (err) {
        console.error("Failed to fetch lists:", err);
      }
    }
  });

  const prepareGames = async () => {
    displayedGames.value = await gameService.getGames(pageNumber.value, gameApiParams.value);
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
        <router-view :games="displayedGames"/>
      </main>
    </div>
  </div>
  <AuthDialog v-if="isAuthOpen" @close="isAuthOpen = false"/>
</template>

<style scoped>

</style>
