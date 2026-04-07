<script setup lang="ts">
  import { onMounted, ref } from 'vue';
  import { gameService } from '@/service/game-service';
  import {Game, GameDetails, GameListRequest, ListType} from '@/types/types';
  import {gameListService} from "@/service/game-list-service";
  import ReviewPanel from "@/components/ReviewPanel.vue";
  import {useAuthStore} from "@/store/auth";
  import {storeToRefs} from "pinia";

  const authStore = useAuthStore();
  const { isAuthenticated } = storeToRefs(authStore);
  const props = defineProps<{
    id: string
  }>();

  const emit = defineEmits(['setNewListItem']);

  const setNewListItem = (game: Game) => {
    emit('setNewListItem', game);
  }

  const game = ref<GameDetails>({
    guid: 0,
    name: '',
    genres: [],
    platforms: [],
    backgroundImage: '',
    releaseDate: new Date(),
    listType: ListType.NONE,
    description: '',
    website: ''
  });

  onMounted(async () => {
    game.value = await gameService.getGameById(Number(props.id));
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });

  const handleAddingToList = async (guid: number, type: ListType) => {
    game.value.listType = type;
    const dto: GameListRequest = {
      guid: guid,
      listType: type,
    }
    const success = await gameListService.modifyLists(dto);
    if (success) setNewListItem(game.value);
  }

</script>

<template>
  <div class="min-h-screen">
    <div class="relative h-96" :style="{ backgroundImage: `url(${game.backgroundImage})` }">
      <div class="absolute inset-0 bg-linear-to-t from-base-100 via-base-100/60 to-transparent" />

      <div class="absolute bottom-0 left-0 right-0 p-8 flex gap-6">
        <img :src="game.backgroundImage" class="w-48 h-64 object-cover rounded-lg shadow-2xl" :alt="game.name"/>

        <div class="flex-1">
          <h1 class="text-4xl font-bold mb-2">{{ game.name }}</h1>

          <div class="flex items-center gap-4 mb-4">
            <span class="text-base-content/60">{{ new Date(game.releaseDate).toLocaleDateString() }}</span>
          </div>

          <div class="flex gap-2">
            <div v-if="isAuthenticated" class="dropdown">
              <label tabindex="0" class="btn btn-primary">
                <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                </svg>
                Add to List
              </label>
              <ul tabindex="0" class="dropdown-content menu bg-base-300 rounded-box w-52 p-2 shadow-lg mt-2">
                <li><a @click="handleAddingToList(game.guid, ListType.WISHLIST)">⭐ Wishlist <span v-if="game.listType === ListType.WISHLIST" class="badge badge-sm">✔️</span></a></li>
                <li><a @click="handleAddingToList(game.guid, ListType.OWNED)">🎯 Owned <span v-if="game.listType === ListType.OWNED" class="badge badge-sm">✔️</span></a></li>
                <li><a @click="handleAddingToList(game.guid, ListType.COMPLETED)">✅ Played <span v-if="game.listType === ListType.COMPLETED" class="badge badge-sm">✔️</span></a></li>
              </ul>
            </div>
            <button class="btn btn-outline"><a target="blank" :href="game.website">Visit Website</a></button>
          </div>
        </div>
      </div>
    </div>

    <div class="container mx-auto p-8 grid grid-cols-3 gap-8">
      <div class="col-span-2 space-y-6">
        <div class="prose prose-invert max-w-none">
          <h1>About</h1>
          <br/>
          <p v-html="game.description"></p>
        </div>

      </div>

      <div class="space-y-4">
        <div class="bg-base-200 rounded-lg p-4">
          <h3 class="font-bold mb-3">Game Info</h3>
          <dl class="space-y-2 text-sm">
            <div class="flex">
              <dt class="text-base-content/60 mr-1 w-15">Platforms</dt>
              <dd>{{ game.platforms?.map(p => p).join(', ') }}</dd>
            </div>
            <div class="flex">
              <dt class="text-base-content/60 mr-1 w-15">Genres</dt>
              <dd>{{ game.genres?.map(g => g).join(', ') }}</dd>
            </div>
          </dl>
        </div>

      </div>
    </div>

    <ReviewPanel :guid="Number.parseInt(id)"/>
  </div>
</template>

<style scoped>

</style>