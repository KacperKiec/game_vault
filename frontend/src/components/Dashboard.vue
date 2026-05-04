<script setup lang="ts">
  import { onMounted, ref, computed } from 'vue';
  import { dashboardService } from '@/service/dashboard-service';
  import { DashboardDocument } from '@/types/types';
  import { useAuthStore } from '@/store/auth';

  const authStore = useAuthStore();
  const dashboard = ref<DashboardDocument | null>(null);

  onMounted(async () => {
    if (authStore.isAuthenticated) dashboard.value = await dashboardService.getDashboard();
  });

  const sortedActivity = computed(() => {
    if (!dashboard.value?.recentActivity) return [];
    return [...dashboard.value.recentActivity].sort((a, b) =>
        new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()
    );
  });

  const formatActivityType = (type: string) => {
    const types: Record<string, string> = {
      'USER_REGISTERED': 'Joined the community',
      'GAME_ADDED_TO_LIST': 'Added to library',
      'GAME_REMOVED_FROM_LIST': 'Removed from library',
      'GAME_MOVED_BETWEEN_LISTS': 'Moved to another list',
      'REVIEW_ADDED': 'Published a review',
      'REVIEW_DELETED': 'Removed a review'
    };
    return types[type] || type.replace('_', ' ').toLowerCase();
  };

  const formatDate = (date: Date | string) => {
    return new Date(date).toLocaleString('en-EN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };
</script>

<template>
  <div v-if="dashboard" class="p-6 space-y-8 max-w-7xl mx-auto">
    <header class="flex flex-col md:flex-row md:items-end justify-between gap-4 border-b border-base-300 pb-6">
      <div>
        <h1 class="text-3xl font-bold text-base-content">Welcome back, {{ dashboard.username }}!</h1>
        <p class="text-base-content/60">Your gaming library overview</p>
      </div>
      <div class="text-sm text-right text-base-content/50 font-mono">
        Last updated: {{ formatDate(dashboard.updatedAt) }}
      </div>
    </header>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="stat bg-base-200 rounded-box border border-base-300">
        <div class="stat-title text-xs uppercase tracking-widest">Wishlist</div>
        <div class="stat-value text-secondary text-2xl">{{ dashboard.stats.wishlistCount }}</div>
      </div>
      <div class="stat bg-base-200 rounded-box border border-base-300">
        <div class="stat-title text-xs uppercase tracking-widest">Playing</div>
        <div class="stat-value text-primary text-2xl">{{ dashboard.stats.playingCount }}</div>
      </div>
      <div class="stat bg-base-200 rounded-box border border-base-300">
        <div class="stat-title text-xs uppercase tracking-widest">Completed</div>
        <div class="stat-value text-accent text-2xl">{{ dashboard.stats.completedCount }}</div>
      </div>
      <div class="stat bg-base-200 rounded-box border border-base-300">
        <div class="stat-title text-xs uppercase tracking-widest">Avg Rating</div>
        <div class="stat-value text-2xl">{{ (dashboard.stats.ratingSum / (dashboard.stats.reviewCount || 1)).toFixed(1) }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">

        <section>
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2">
            <span class="w-2 h-6 bg-primary rounded-full"></span> Recent Activity
          </h2>
          <div class="bg-base-200 rounded-box overflow-hidden border border-base-300">
            <table class="table w-full">
              <tbody>
              <tr v-for="(activity, index) in sortedActivity" :key="index" class="hover:bg-base-300/50">
                <td class="w-12 text-center">
                  <div class="avatar placeholder">
                    <div class="bg-neutral text-neutral-content rounded-full w-8">
                      <span class="inline-block text-m mt-1">{{ activity.type.substring(0, 1) }}</span>
                    </div>
                  </div>
                </td>
                <td>
                  <span class="font-bold text-primary text-sm">{{ activity.gameTitle }}</span>
                  <p class="text-[11px] font-medium text-base-content/70 italic">
                    {{ formatActivityType(activity.type) }}
                  </p>
                </td>
                <td class="text-right text-[11px] font-mono text-base-content/50">
                  {{ formatDate(activity.occurredAt) }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </section>

        <section>
          <h2 class="text-xl font-bold mb-4 flex items-center gap-2">
            <span class="w-2 h-6 bg-accent rounded-full"></span> Latest Reviews
          </h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div v-for="review in dashboard.latestReviews" :key="review.reviewId"
                 class="card bg-base-300 shadow-xl border transition-all duration-300"
                 :class="[
                   review.isDeleted
                   ? 'border-error/20 grayscale opacity-60 bg-base-200/50'
                   : 'border-base-300 hover:border-accent/30'
                 ]"
            >
              <div class="card-body p-5">
                <div class="flex justify-between items-start">
                  <div class="flex flex-col">
                    <h3 class="card-title text-sm" :class="{ 'line-through opacity-50': review.isDeleted }">
                      {{ review.gameTitle }}
                    </h3>
                    <div v-if="review.isDeleted" class="badge badge-error badge-outline badge-xs mt-1">Deleted</div>
                  </div>

                  <div class="badge" :class="review.isDeleted ? 'badge-ghost opacity-30' : 'badge-accent'">
                    {{ review.isDeleted ? '—' : `${review.rating}/5` }}
                  </div>
                </div>

                <p class="text-sm italic mt-2 line-clamp-2"
                   :class="review.isDeleted ? 'text-error/40 italic' : 'text-base-content/70'">
                  {{ review.isDeleted ? 'This review has been removed.' : `"${review.reviewPreview}"` }}
                </p>

                <div class="card-actions justify-end mt-4">
                  <span class="text-[10px] font-mono opacity-40 italic">
                    {{ formatDate(review.createdAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>

      <div class="space-y-6">
        <section class="bg-base-200 p-4 rounded-box border border-base-300">
          <h2 class="text-lg font-bold mb-4 px-2 text-base-content">Library Quick View</h2>

          <div class="space-y-2">
            <details class="collapse collapse-arrow bg-base-300 rounded-lg group">
              <summary class="collapse-title text-xs font-bold uppercase tracking-widest text-secondary min-h-0 py-3">
                Wishlist ({{ dashboard.listsPreview.wishlist.length }})
              </summary>
              <div class="collapse-content">
                <ul class="space-y-1 pt-2">
                  <li v-for="game in dashboard.listsPreview.wishlist" :key="game.gameId"
                      class="text-[13px] p-2 pl-3 border-l-2 border-secondary/30 hover:border-secondary hover:bg-base-100 rounded-r-md transition-all cursor-default">
                    <span class="truncate">{{ game.title }}</span>
                  </li>
                </ul>
              </div>
            </details>

            <details class="collapse collapse-arrow bg-base-300 rounded-lg group">
              <summary class="collapse-title text-xs font-bold uppercase tracking-widest text-primary min-h-0 py-3">
                Currently Playing ({{ dashboard.listsPreview.owned.length }})
              </summary>
              <div class="collapse-content">
                <ul class="space-y-1 pt-2">
                  <li v-for="game in dashboard.listsPreview.owned" :key="game.gameId"
                      class="flex items-center gap-2 text-[13px] p-2 pl-3 border-l-2 border-primary/30 hover:border-primary hover:bg-base-100 rounded-r-md transition-all cursor-default">
                    <span class="font-medium truncate">{{ game.title }}</span>
                  </li>
                </ul>
              </div>
            </details>

            <details class="collapse collapse-arrow bg-base-300 rounded-lg group">
              <summary class="collapse-title text-xs font-bold uppercase tracking-widest text-accent min-h-0 py-3">
                Completed ({{ dashboard.listsPreview.completed.length }})
              </summary>
              <div class="collapse-content">
                <ul class="space-y-1 pt-2">
                  <li v-for="game in dashboard.listsPreview.completed" :key="game.gameId"
                      class="flex justify-between items-center text-[13px] p-2 pl-3 border-l-2 border-accent/30 hover:border-accent hover:bg-base-100 rounded-r-md transition-all cursor-default">
                    <span class="truncate">{{ game.title }}</span>
                  </li>
                </ul>
              </div>
            </details>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>