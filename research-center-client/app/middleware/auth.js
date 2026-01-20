export default defineNuxtRouteMiddleware((to, from) => {
    const auth = useAuth()
    auth.initAuth()

    if (!auth.token.value) {
        return navigateTo('/auth/login')
    }
})
