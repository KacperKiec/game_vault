<script setup lang="ts">

import Register from "@/components/auth/Register.vue";
import Login from "@/components/auth/Login.vue";
import { ref } from 'vue';

const emit = defineEmits(['close']);

const close = () => {
  emit('close');
  console.log("close")
};

const showLogin = ref(true);

</script>

<template>
  <div class="fixed inset-0 z-100 flex justify-center items-center bg-black/20 backdrop-blur-sm"
       @click.self="close">

    <div class="bg-base-200 shadow-2xl rounded-xl border border-primary p-6 w-sm min-h-80 flex-col justify-center items-center relative">

      <div class="mb-4">
        <div class="absolute left-4 top-4">
          <div class="join">
            <button
                @click="showLogin = true"
                class="btn btn-xs join-item"
                :class="showLogin ? 'btn-primary' : 'btn-ghost'"
            >
              Login
            </button>
            <button
                @click="showLogin = false"
                class="btn btn-xs join-item"
                :class="!showLogin ? 'btn-primary' : 'btn-ghost'"
            >
              Register
            </button>
          </div>
        </div>
      </div>

      <button
          @click="close"
          class="btn btn-sm btn-circle btn-ghost absolute right-2 top-2">
        ✕
      </button>

      <Login v-if="showLogin" @close="$emit('close')"/>
      <Register v-else @close="$emit('close')"/>
    </div>

  </div>
</template>

<style scoped>

</style>