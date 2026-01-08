import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import AccountView from '../views/AccountView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/view-posts',
      name: 'PostsView',
      component: () => import('../views/PostsView.vue')
    },
    {
      path: '/account',
      name: 'AccountView',
      component: AccountView
    },
    {
      path: '/friend-requests',
      name: 'FriendRequests',
      component: () => import('../views/FriendRequestsView.vue')
    },
    {
      path: '/friends-list',
      name: 'FriendsList',
      component: () => import('../views/FriendsListView.vue')
    },
    {
      path: '/profile/:username',
      name: 'ProfileView',
      component: () => import('../views/ProfileView.vue'),
      props: true,
    },
    {
      path: '/create-post',
      name: 'CreatePost',
      component: () => import('../views/CreatePostView.vue')
    },
    {
      path: '/create-user',
      name: 'CreateUser',
      component: () => import('../views/CreateUserView.vue')
    },
    {
      path: '/topics',
      name: 'topics',
      component: () => import('../views/TopicsView.vue')
    },
    {
      path: '/topics/:topic',
      name: 'TopicPosts',
      component: () => import('../views/TopicPostsView.vue'),
      props: true
    }
  ],
})

export default router