<script setup lang="ts">
  import {ListType, GameParams} from "@/types/types";
  import {onMounted, ref} from "vue";
  import {storeToRefs} from "pinia";
  import {useAuthStore} from "@/store/auth";
  import {gameService} from "@/service/game-service";
  import Slider from '@vueform/slider';
  import '@vueform/slider/themes/default.css';

  defineProps<{
    wishlistSize: number,
    toPlaySize: number,
    completedSize: number
  }>();

  const emit = defineEmits(['setGameList', 'setGameName', 'setGameParams', 'setDates']);

  const setGameList = (listType: ListType) => {
    activeList.value = listType;
    emit('setGameList', listType);
  };

  const setGameName = () => {
    emit('setGameName', gameName.value);
  }

  const setGameParams = () => {
    emit('setGameParams', selectedParams.value);
  }

  const setDates = (dates: string) => {
    emit('setDates', dates);
  }

  const authStore = useAuthStore();
  const activeList = ref(ListType.NONE)
  const { isAuthenticated } = storeToRefs(authStore);

  const gameName = ref('');
  const params = ref<GameParams>({ genres: [], platforms: [] });
  const selectedParams = ref<GameParams>({ genres: [], platforms: [] });
  const yearRange = ref([1980, 2026]);

  onMounted(async () => {
    try {
      params.value = await gameService.getGameParams();
    } catch (err) {
      console.error("Failed to fetch params:", err);
    }
  });

  const handleYearChange = (value: number[]) => {
    const dates = `${value[0]}-01-01,${value[1]}-12-31`;
    setDates(dates);
  };
</script>

<template>
  <aside class="w-64 bg-base-200 p-4 rounded-lg space-y-4">
    <div class="form-control">
      <label class="input input-bordered flex items-center gap-2 bg-base-300">
        <svg @click="setGameName" class="w-4 h-4 opacity-50 cursor-pointer" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
        <input type="text" v-model="gameName" @keyup.enter="setGameName" class="grow bg-transparent" placeholder="Search games..." />
      </label>
    </div>

    <div class="menu bg-base-300 rounded-box w-full">
      <ul v-if="isAuthenticated">
        <li class="menu-title">My Lists</li>
        <li><a :class="{ 'active': activeList === ListType.NONE }" @click="setGameList(ListType.NONE)">📂 All Games</a></li>
        <li><a :class="{ 'active': activeList === ListType.WISHLIST }" @click="setGameList(ListType.WISHLIST)">⭐ Wishlist <span class="badge badge-sm">{{ wishlistSize }}</span></a></li>
        <li><a :class="{ 'active': activeList === ListType.TODO }" @click="setGameList(ListType.TODO)">🎯 To Play <span class="badge badge-sm">{{ toPlaySize }}</span></a></li>
        <li><a :class="{ 'active': activeList === ListType.COMPLETED }" @click="setGameList(ListType.COMPLETED)">✅ Played <span class="badge badge-sm">{{ completedSize }}</span></a></li>
      </ul>
      <p v-else>Log in to see your game lists</p>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" checked />
      <div class="collapse-title font-medium">Platform</div>

      <div class="collapse-content p-0">
        <div class="max-h-48 overflow-y-auto px-4 pb-4 custom-scrollbar">
          <div class="flex flex-col gap-1">
            <label v-for="platform in params.platforms" :key="platform.slug"
                   class="label cursor-pointer hover:bg-base-100 rounded-lg px-2 transition-colors flex justify-between">
              <span class="label-text text-sm">{{ platform.name }}</span>
              <input type="checkbox" class="checkbox checkbox-primary checkbox-sm" :value="platform.slug" v-model="selectedParams.platforms" @change="setGameParams"/>
            </label>
          </div>
        </div>
      </div>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" />
      <div class="collapse-title font-medium">Genre</div>

      <div class="collapse-content p-0">
        <div class="max-h-48 overflow-y-auto px-4 pb-4 custom-scrollbar">
          <div class="flex flex-col gap-1">
            <label v-for="genre in params.genres" :key="genre.slug"
                   class="label cursor-pointer hover:bg-base-100 rounded-lg px-2 transition-colors flex justify-between">
              <span class="label-text text-sm">{{ genre.name }}</span>
              <input type="checkbox" class="checkbox checkbox-primary checkbox-sm" :value="genre.slug" v-model="selectedParams.genres" @change="setGameParams"/>
            </label>
          </div>
        </div>
      </div>
    </div>

    <div class="collapse collapse-arrow bg-base-300">
      <input type="checkbox" />
      <div class="collapse-title font-medium">Release Year</div>
      <div class="collapse-content pt-4 mt-2"> <div class="px-2">
        <Slider
            v-model="yearRange"
            :min="1980"
            :max="2026"
            :step="1"
            @change="handleYearChange"
            class="game-slider"
        />
      </div>

        <div class="flex justify-between text-xs text-base-content/60 mt-6">
          <span>From: <b>{{ yearRange[0] }}</b></span>
          <span>To: <b>{{ yearRange[1] }}</b></span>
        </div>
      </div>
    </div>

  </aside>
</template>

<style scoped>

</style>