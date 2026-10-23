import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)
const QuestionView = () => import('../views/element/QuestionView.vue')
export default new VueRouter({
  mode: 'hash',
  routes: [
    { path: '/', redirect: '/user' },
    { path: '/user', name: 'user', component: () => import('../views/element/UserView.vue') },
    { path: '/question', name: 'question', component: QuestionView },
    { path: '/question/add', name: 'question-add', component: QuestionView },
    { path: '*', redirect: '/user' }
  ]
})
