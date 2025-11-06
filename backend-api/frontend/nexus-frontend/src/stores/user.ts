import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null as any,
        error: null as string | null,
    }),

    actions: {
        async login(username: string, password: string) {
            this.error = null
            try {
                const res = await fetch('/api/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ username, password }),
                    credentials: 'include',
                })

                if (!res.ok) {
                    const text = await res.text()
                    throw new Error(text || `Login failed with status ${res.status}`)
                }

                const data = await res.json()
                this.user = data.username || username
                this.error = null
                return data
            } catch (err: any) {
                this.error = err.message || String(err)
                this.user = null
                throw err
            }
        }

    },
})
