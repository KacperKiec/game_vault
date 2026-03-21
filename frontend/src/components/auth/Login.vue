<script setup lang="ts">

import { ref } from 'vue';
import { authService} from "@/service/auth-service";
import { AuthRequest } from "@/types/types";
import {useRouter} from "vue-router";

const emit = defineEmits(['close']);
const router = useRouter();

const username = ref('');
const password = ref('');
const showTip = ref(false);

const handleLogin = async () => {
  if (username.value.trim() !== '' && password.value.trim() !== '') {

    try {
      const dto: AuthRequest = {
        username: username.value,
        password: password.value
      }

      const success = await authService.login(dto);

      if (success) {
        emit('close');
        await router.push('/');
      }
    } catch (err) {
      console.error(err);
    }

  } else {
    showTip.value = true;
  }
}

</script>

<template>
  <div class="flex flex-col justify-center items-center gap-4 w-full">
    <h2 class="font-bold text-2xl text-primary mb-2">Log In</h2>

    <label for="username" class="label font-semibold">Username</label>
    <input id="username" type="text" placeholder="Username" v-model="username" class="input input-bordered input-accent w-full rounded-xl">

    <label for="pass" class="label font-semibold">Password</label>
    <input id="pass" type="password" placeholder="Password" v-model="password" class="input input-bordered input-accent w-full rounded-xl">

    <p v-if="showTip" class="text-error text-xs">Fill in your login details</p>
    <button @click="handleLogin" class="btn btn-primary btn-sm rounded-md px-6 mt-2">Confirm</button>
  </div>
</template>

<style scoped>

</style>