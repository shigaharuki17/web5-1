import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)
// 同时兼容直接页面地址和静态托管使用的 hash 地址。
const directRoute = window.location.pathname.match(/^(.*?)(\/(?:user|question(?:\/add)?))\/?$/)
if (!window.location.hash && directRoute) {
  window.history.replaceState(null, '', `${directRoute[1]}/#${directRoute[2]}${window.location.search}`)
}
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
