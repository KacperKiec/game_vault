<script setup lang="ts">
import {ref, watch, computed, onUnmounted} from 'vue';
  import { useRouter } from 'vue-router';
  import { storeToRefs } from "pinia";
  import { useAuthStore } from "@/store/auth";
  import { notificationSocketService } from "@/service/notification-socket-service";
  import { notificationService } from "@/service/notification-service";
  import { AppNotification } from "@/types/types";

  const router = useRouter();
  const authStore = useAuthStore();
  const { isAuthenticated } = storeToRefs(authStore);

  const notifications = ref<AppNotification[]>([]);

  const hasUnread = computed(() => {
    return notifications.value.some(n => !n.read);
  });

  const fetchHistory = async () => {
    if (!isAuthenticated.value) return;
    try {
      const data = await notificationService.getNotifications();
      notifications.value = data.map((n: any): AppNotification => ({
        id: n.id,
        type: n.type,
        title: n.title,
        content: n.content,
        metadata: n.metadata || {},
        createdAt: new Date(n.createdAt),
        read: n.read ?? false
      }));
    } catch (error) {
      console.error("Failed to fetch notification history:", error);
    }
  };

  watch(() => notificationSocketService.latestNotification.value, (newVal) => {
    if (newVal) {
      const notificationToAdd: AppNotification = {
        id: newVal.id,
        type: newVal.type,
        title: newVal.title,
        content: newVal.content,
        metadata: newVal.metadata || {},
        createdAt: new Date(newVal.createdAt),
        read: false
      };

      if (!notifications.value.some(n => n.id === notificationToAdd.id)) {
        notifications.value.unshift(notificationToAdd);
      }
    }
  });

  const selectNotification = async (notification: AppNotification) => {
    if (document.activeElement instanceof HTMLElement) {
      document.activeElement.blur();
    }

    if (notification.type === 'NEW_REVIEW' && notification.metadata?.guid) {
      await router.push(`/details/${notification.metadata.guid}`);
    }

    if (!notification.read) {
      try {
        await notificationService.markNotificationAsRead(notification.id);
        notification.read = true;

      } catch (error) {
        console.error("Error marking as read:", error);
      }
    }
  };

  watch(isAuthenticated, (isLoggedIn) => {
    if (isLoggedIn) {
      notificationSocketService.connect();
      fetchHistory();
    } else {
      notificationSocketService.disconnect();
      notifications.value = [];
    }
  }, { immediate: true });

  onUnmounted(() => {
    notificationSocketService.disconnect();
    console.log('disconnected');
  });

  const formatTimeAgo = (date: Date) => {
    const seconds = Math.floor((new Date().getTime() - date.getTime()) / 1000);
    if (seconds < 60) return "just now";
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    return date.toLocaleDateString();
  };
</script>

<template>
  <div class="dropdown dropdown-end">
    <label tabindex="0" class="btn btn-ghost btn-circle">
      <span class="indicator">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
        <span v-if="hasUnread" class="indicator-item badge badge-error badge-xs animate-pulse"></span>
      </span>
    </label>

    <div tabindex="0" class="dropdown-content z-50 mt-3 w-80 sm:w-96 card card-compact bg-base-200 shadow-xl border border-base-300">
      <div class="card-body border-b border-base-300 py-3">
        <div class="flex items-center justify-between">
          <h2 class="card-title text-base font-bold">Notifications</h2>
          <span class="badge badge-primary badge-sm">{{ notifications.filter(n => !n.read).length }} new</span>
        </div>
      </div>

      <div class="max-h-96 overflow-y-auto overscroll-contain">
        <div v-if="notifications.length === 0" class="text-center py-10 text-base-content/40">
          <p class="text-sm">No notifications yet 💤</p>
        </div>

        <div
            v-for="notification in notifications"
            :key="notification.id"
            @click="selectNotification(notification)"
            class="p-4 flex gap-4 hover:bg-base-300 transition-all cursor-pointer border-b border-base-300 last:border-b-0"
            :class="{'bg-primary/5': !notification.read}"
        >
          <div class="shrink-0">
            <div class="avatar placeholder">
              <div
                  class="rounded-full w-10 h-10 border border-base-300 transition-colors"
                  :class="notification.read ? 'bg-base-300 text-base-content/50' : 'bg-primary/20 text-primary'"
              >
                <span class="inline-block text-[10px] font-bold ml-2.5 mt-3">{{ notification.type === 'NEW_REVIEW' ? 'REV' : 'MSG' }}</span>
              </div>
            </div>
          </div>

          <div class="grow">
            <p class="text-sm text-base-content" :class="{'font-bold': !notification.read}">
              {{ notification.title }}
            </p>
            <p class="text-xs text-base-content/70 line-clamp-2 mt-0.5">
              {{ notification.content }}
            </p>
            <p class="text-[10px] text-base-content/40 mt-1 uppercase tracking-wider">
              {{ formatTimeAgo(notification.createdAt) }}
            </p>
          </div>

          <div v-if="!notification.read" class="shrink-0 self-center">
            <div class="w-2.5 h-2.5 bg-primary rounded-full shadow-[0_0_8px_rgba(99,102,241,0.6)]"></div>
          </div>
        </div>
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

.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: var(--base-300);
  border-radius: 10px;
}
</style>