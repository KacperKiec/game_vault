<script setup lang="ts">

import {ref} from "vue";
import { authService} from "@/service/auth-service";
import {UserRegister} from "@/types/types";
import {useRouter} from "vue-router";

const emit = defineEmits(['close']);
const router = useRouter();

const email = ref('');
const username = ref('');
const password = ref('');
const repeatPassword = ref('');
const showTip = ref(false);
const tip = ref('');
const emailFeedback = ref('');

const validatePassword = (password: string, repeat: string) => {
  if (password.length < 8) {
    tip.value = 'Password must be at least 8 characters long';
    showTip.value = true;
    return false;
  } else if (password != repeat) {
    tip.value = 'Repeat password properly';
    showTip.value = true;
    return false;
  }
  return true;
}

const handleRegister = async () => {
  emailFeedback.value = '';
  showTip.value = false;
  tip.value = '';

  if (username.value.trim() !== '' && email.value.trim() !== '') {
    if (validatePassword(password.value, repeatPassword.value)) {
      try {
        const dto: UserRegister = {
          email: email.value,
          password: password.value,
          username: username.value
        }

        const success = await authService.register(dto);

        if (success) {
          emit('close');
          await router.push('/');
        }
      } catch (err) {
        if (err instanceof Error) {
          emailFeedback.value = err.message;
        } else {
          emailFeedback.value = "An unexpected error occurred";
        }
        console.error(err);
      }
    } else {
      showTip.value = true;
    }
  }
}

</script>

<template>
  <div class="flex flex-col justify-center items-center gap-4 w-full">
    <h2 class="font-bold text-2xl text-primary mb-2">Register</h2>

    <label for="email" class="label font-semibold">Email</label>
    <input id="email" type="text" placeholder="Email" v-model="email" class="input input-bordered input-accent w-full rounded-xl">
    <p v-if="emailFeedback.length > 0" class="text-error text-xs">{{ emailFeedback }}</p>

    <label for="username" class="label font-semibold">Username</label>
    <input id="username" type="text" placeholder="Username" v-model="username" class="input input-bordered input-accent w-full rounded-xl">

    <label for="pass" class="label font-semibold">Password</label>
    <input id="pass" type="password" placeholder="Password" v-model="password" class="input input-bordered input-accent w-full rounded-xl">

    <label for="repeat" class="label font-semibold">Repeat Password</label>
    <input id="repeat" type="password" placeholder="Repeat Password" v-model="repeatPassword" class="input input-bordered input-accent w-full rounded-xl">

    <p v-if="showTip" class="text-error text-xs">{{ tip }}</p>
    <button @click="handleRegister" class="btn btn-primary btn-sm rounded-md px-6 mt-3">Confirm</button>
  </div>
</template>

<style scoped>

</style>