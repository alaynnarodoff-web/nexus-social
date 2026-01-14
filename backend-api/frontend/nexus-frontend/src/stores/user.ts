import { defineStore } from 'pinia'
import router from '@/router'

export interface UserProfile {
    username: string
    email: string
    firstName: string
    lastName: string
    phone: string
    bio: string
    avatar: string
}

export const useUserStore = defineStore('user', {
    state: () => ({
        user: null as UserProfile | null,
        friendCount: 0,
        error: null as string | null,
        fof: [] as UserProfile[],
    }),

    actions: {
        async login(username: string, password: string) {
            this.error = null
            try {
                const res = await fetch('/api/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password }),
                    credentials: 'include',
                })

                if (!res.ok) {
                    const text = await res.text()
                    throw new Error(text || `Login failed with status ${res.status}`)
                }

                await this.fetchUser()
                await this.fetchFriendCount()

                this.error = null
                return this.user

            } catch (err: any) {
                this.error = err.message || String(err)
                this.user = null
                throw err
            }
        },

        async fetchUser() {
            try {
                const response = await fetch('/api/getUser', { method: 'GET' })

                if (response.status === 401) {
                    this.user = null
                    sessionStorage.removeItem('loggedInUser')
                    router.push('/login')
                    return
                }

                if (!response.ok) throw new Error('Could not fetch user data')

                const user: UserProfile = await response.json()
                this.user = user
            } catch (err: any) {
                this.error = err.message
                this.user = null
            }
        },

        async fetchUserByUsername(username: string) {
            try {
                const response = await fetch(`/api/users/${username}`, { method: 'GET' })


                if (!response.ok) throw new Error('User not found')
                return await response.json()
            } catch (err: any) {
                console.error(err)
                return null
            }
        },

        async fetchFriendCount(username?: string) {
            try {
                const url = username
                    ? `/api/friends/count?username=${encodeURIComponent(username)}`
                    : '/api/friends/count';

                const res = await fetch(url);
                if (!res.ok) throw new Error('Failed to get count');

                const count = await res.text();
                const numericCount = parseInt(count, 10) || 0;

                if (!username || username === this.user?.username) {
                    this.friendCount = numericCount;
                }

                return numericCount;
            } catch (err) {
                console.error(err);
                return 0;
            }
        },

        async updateUser(userData: Partial<UserProfile>) {
            const response = await fetch('/api/updateUser', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData),
            })

            if (response.status === 401) {
                alert("Session expired. Please log in again.")
                router.push('/login')
                return
            }

            if (!response.ok) {
                throw new Error('Failed to update user')
            }

            await this.fetchUser()
        },

        async getFriendCountForUser(username: string): Promise<number> {
            try {
                const res = await fetch(`/api/friends/count?username=${username}`);
                if (!res.ok) return 0;
                const count = await res.text();
                return parseInt(count, 10) || 0;
            } catch {
                return 0;
            }
        },

        async logout() {
            try {
                await fetch('/api/logout', {
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (err) {
                console.error("Server-side logout failed:", err);
            } finally {
                this.user = null;
                this.friendCount = 0;
                this.fof = [];
                sessionStorage.removeItem('loggedInUser');
                router.replace('/login');
            }
        },
        async fetchFof(username?: string) {
            this.error = null
            try {
                const res = await fetch(`/api/fof/${username}`, {
                    method: 'GET',
                    credentials: 'include',
                })
                this.fof = await res.json()

            }
            catch (err: any) {
                this.error = err.message || String(err)
            }
        },
    },
})