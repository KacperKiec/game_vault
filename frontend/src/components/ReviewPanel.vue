<script setup lang="ts">

import {Review, ReviewRequest} from "@/types/types";
import {onMounted, ref, watch} from "vue";
  import {useAuthStore} from "@/store/auth";
  import {storeToRefs} from "pinia";
  import {reviewService} from "@/service/review-service";
  const authStore = useAuthStore();
  const { isAuthenticated, username } = storeToRefs(authStore);

  const props = defineProps<{
    guid: number;
    name: string;
  }>();

  const reviews = ref<Review[]>([]);
  const userReview = ref<Review | null>(null);
  const newReview = ref<ReviewRequest>({
    guid: props.guid,
    gameName: props.name,
    content: '',
    rating: 5
  });

  watch(() => props.name, (newName) => {
    if (newName) {
      newReview.value.gameName = newName;
    }
  }, { immediate: true });

  const handleRemove = async () => {
    if (!userReview.value) return;
    const success = await reviewService.deleteReview(userReview.value?.id);
    if (success) {
      reviews.value = reviews.value.filter(r => r.username !== username.value);
      userReview.value = null;
      newReview.value.content = '';
      newReview.value.rating = 5;
    }
  };

  const handleAddReview = async () => {
    const response = await reviewService.addReview(newReview.value);
    if (response) {
      console.log(response);
      userReview.value = response;

      const contains = reviews.value.find(r => r.id === response.id);
      if (!contains) {
        reviews.value.push(response);
      }
    }
  }

  onMounted(async () => {
    reviews.value = await reviewService.getReviews(props.guid);
    if (isAuthenticated) {
      const r = reviews.value.find(r => r.username === username.value);
      r != null ? userReview.value = r : null;
    }
  });

</script>

<template>
  <div class="max-w-4xl mx-auto w-full space-y-8 p-4">

    <section v-if="isAuthenticated" class="bg-base-300 rounded-2xl p-6 shadow-inner border border-base-100">
      <h2 class="text-xl font-bold mb-4 flex items-center gap-2">
        <span class="text-primary text-2xl">★</span> Your Review
      </h2>

      <div v-if="userReview" class="relative group bg-base-100 p-4 rounded-xl border border-primary/20">
        <button
            @click="handleRemove()"
            class="absolute top-2 right-2 btn btn-ghost btn-xs btn-circle text-error opacity-0 group-hover:opacity-100 transition-opacity"
        >
          ✕
        </button>

        <div class="flex items-center gap-2 mb-2">
          <div class="rating rating-sm">
            <input v-for="i in 5" :key="i" type="radio" class="mask mask-star-2 bg-orange-400" :checked="i === userReview.rating" disabled />
          </div>
          <span class="text-xs opacity-50 uppercase font-bold tracking-widest">Posted</span>
        </div>

        <p class="text-base-content/80 italic">"{{ userReview.content }}"</p>
      </div>

      <div v-else class="space-y-4">
        <div class="flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div class="rating rating-md">
            <input v-for="i in 5" :key="i" type="radio" name="rating-add" class="mask mask-star-2 bg-orange-400" v-model="newReview.rating" :value="i" />
          </div>
          <span class="text-sm opacity-60">Set Rating</span>
        </div>

        <div class="flex gap-2">
          <textarea
              v-model="newReview.content"
              class="textarea textarea-bordered textarea-primary w-full bg-base-200 focus:bg-base-100 transition-colors"
              placeholder="Type what you think..."
              rows="2"
          ></textarea>
          <button @click="handleAddReview" class="btn btn-primary h-auto min-h-full aspect-square">
            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 rotate-90" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" /></svg>
          </button>
        </div>
      </div>
    </section>

    <section class="space-y-4">
      <div class="flex justify-between items-center border-b border-base-300 pb-2">
        <h3 class="font-semibold text-lg uppercase tracking-wider">Community Reviews</h3>
        <span class="badge badge-ghost">{{ reviews.length }} posts</span>
      </div>

      <div v-if="reviews.length > 0" class="grid gap-4">
        <div v-for="review in reviews" :key="review.id"
             class="bg-base-200 p-5 rounded-xl flex gap-4 hover:bg-base-300/50 transition-colors">

          <div class="avatar placeholder">
            <div class="bg-neutral text-neutral-content rounded-full w-12 h-12">
              <span class="inline-block text-xl ml-4 mt-2">{{ review.username.charAt(0).toUpperCase() }}</span>
            </div>
          </div>

          <div class="flex-1">
            <div class="flex justify-between items-start">
              <span class="font-bold text-primary">{{ review.username }}</span>
              <span class="text-sm text-base-content/60">{{ review.date }}</span>
              <div class="rating rating-xs">
                <input v-for="i in 5" :key="i" type="radio" class="mask mask-star-2 bg-orange-400" :checked="i === review.rating" disabled />
              </div>
            </div>
            <p class="text-sm mt-2 leading-relaxed text-base-content/70">
              {{ review.content }}
            </p>
          </div>
        </div>
      </div>

      <div v-else class="text-center py-12 opacity-30 italic">
        There are no reviews yet. Add one!
      </div>
    </section>

  </div>
</template>

<style scoped>
  textarea::-webkit-scrollbar {
    width: 8px;
  }
  textarea::-webkit-scrollbar-thumb {
    background: hsl(var(--p));
    border-radius: 10px;
}
</style>