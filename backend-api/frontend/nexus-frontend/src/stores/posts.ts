import { defineStore } from 'pinia'
import { useUserStore } from './user'
import router from '@/router'

export interface Comment {
    author: string;
    authorAvatar: string;
    text: string;
    timestamp: string;
}

export interface Post {
    id: string;
    authorId: string;
    content: string;
    imageUrl: string;
    authorAvatar: string;
    timestamp: string;
    imageBase64?: string;
    likedBy: string[];
    comments: Comment[];
    showComments: boolean;
}

export const usePostsStore = defineStore('posts', {
    state: () => ({
        posts: [] as Post[],
        error: null as string | null,
        loading: false,
    }),

    actions: {
        async getNewsFeed() {
            this.loading = true
            try {
                const res = await fetch('/api/posts', { credentials: 'include' })

                if (res.status === 401) {
                    router.push('/login')
                    return
                }

                if (!res.ok) throw new Error('Failed to load posts.')
                const data = await res.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err: any) {
                console.error(err)
            } finally {
                this.loading = false
            }
        },

        async getFriendsFeed() {
            this.loading = true
            try {
                const response = await fetch('/api/posts/friends', { credentials: 'include' })

                if (response.status === 401) {
                    router.push('/login')
                    return
                }

                if (!response.ok) throw new Error('Failed to fetch friends feed')
                const data = await response.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err: any) {
                this.error = err.message
            } finally {
                this.loading = false
            }
        },

        async getFriendsOfFriendsFeed() {
            this.loading = true
            try {
                const response = await fetch('/api/posts/fof', { credentials: 'include' })

                if (response.status === 401) {
                    router.push('/login')
                    return
                }

                if (!response.ok) throw new Error('Failed to fetch friends of friends feed')
                const data = await response.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err: any) {
                this.error = err.message
            } finally {
                this.loading = false
            }
        },

        async fetchUserPosts(username: string) {
            this.loading = true
            this.posts = []
            try {
                const res = await fetch(`/api/posts/user/${username}`, { credentials: 'include' })
                if (!res.ok) throw new Error('Failed to load user posts')
                const data = await res.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err: any) {
                this.error = err.message
            } finally {
                this.loading = false
            }
        },

        async toggleLike(postId: string) {
            try {
                const response = await fetch(`/api/posts/${postId}/like`, {
                    method: 'POST',
                    credentials: 'include'
                })

                if (response.status === 401) {
                    alert("Please log in to like this post.")
                    router.push('/login')
                    return
                }

                if (!response.ok) throw new Error('Failed to like post')

                const updatedPost = await response.json()

                const index = this.posts.findIndex(p => p.id === postId)
                if (index !== -1) {
                    const showCommentsState = this.posts[index]!.showComments
                    this.posts[index] = { ...updatedPost, showComments: showCommentsState }
                }
            } catch (err) {
                console.error("Like failed:", err)
            }
        },

        async addComment(postId: string, text: string) {
            const userStore = useUserStore()
            const payload = {
                author: userStore.user?.username,
                text: text
            }

            try {
                const response = await fetch(`/api/posts/${postId}/comment`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload),
                    credentials: 'include'
                })

                if (response.status === 401) {
                    alert("Please log in to comment.")
                    router.push('/login')
                    return
                }

                if (!response.ok) throw new Error('Failed to post comment')

                const updatedPost = await response.json()

                const index = this.posts.findIndex(p => p.id === postId)
                if (index !== -1) {
                    const showCommentsState = this.posts[index]!.showComments
                    this.posts[index] = { ...updatedPost, showComments: showCommentsState }
                }
            } catch (err) {
                console.error("Comment failed:", err)
            }
        },

        // --- MOVED INSIDE THE ACTIONS BLOCK ---
        async fetchPostsByTopic(topic: string) {
            this.loading = true;
            this.posts = []
            try {
                const res = await fetch(`/api/posts/topic/${topic}`, { credentials: 'include' })

                if (!res.ok) throw new Error('Failed to load topic posts')

                const data = await res.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err: any) {
                this.error = err.message
                console.error(err)
            } finally {
                this.loading = false
            }
        }
        // --- END OF MOVED FUNCTION ---

    }, // <--- ACTIONS CLOSING BRACE IS HERE NOW
})