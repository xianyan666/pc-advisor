import { ref } from 'vue'
import { defineStore } from 'pinia'
import { listUsers, getUserById, addUser } from '@/api/user'
import type { User } from '@/types'

export const useUserStore = defineStore('user', () => {
  const users = ref<User[]>([])
  const currentUser = ref<User | null>(null)
  const loading = ref(false)

  async function fetchUsers() {
    loading.value = true
    try {
      const res = await listUsers()
      users.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchUserById(id: number) {
    loading.value = true
    try {
      const res = await getUserById(id)
      currentUser.value = res.data
    } finally {
      loading.value = false
    }
  }

  async function createUser(data: User) {
    await addUser(data)
    await fetchUsers()
  }

  return { users, currentUser, loading, fetchUsers, fetchUserById, createUser }
})
