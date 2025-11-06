import { defineStore } from 'pinia'

export interface Post {
    id: String;
    authorId: String;
    content: String;
    imageUrl: String;
    authorAvatar: String;
    timestamp: String;
    imageBase64?: String;
    likedBy: String[];
    comments: Comment[];
    showComments: boolean;
}
export interface Comment {
    id: String;
    postId: String;
    authorId: String;
    content: String;
    timestamp: String;
    authorAvatar: String;
}

export const usePostsStore = defineStore('posts', {
    state: () => ({
        posts: [] as Post[],
        error: null as string | null,
        loading: Boolean(false),
    }),

    actions: {
        async getNewsFeed() {
            this.loading = true
            const url = '/api/posts'
            try {
                const res = await fetch(url, { credentials: 'include' })
                if (!res.ok) throw new Error('Failed to load posts.')
                const data = await res.json()
                this.posts = data.map((p: any) => ({ ...p, showComments: false }))
            } catch (err) {
                console.error(err)
            } finally {

                this.loading = false
            }

        }

    },
})
