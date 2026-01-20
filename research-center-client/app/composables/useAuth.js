export const useAuth = () => {
    // Use useCookie for persistence across SSR and Client
    const token = useCookie('auth_token', {
        maxAge: 60 * 60 * 24 * 7, // 7 days
        sameSite: 'lax',
        secure: false // Set to true in production if HTTPS
    })
    
    const user = useCookie('auth_user', {
         maxAge: 60 * 60 * 24 * 7,
         sameSite: 'lax',
         secure: false,
         default: () => null
    })

    const login = (newToken, newUser) => {
        token.value = newToken
        user.value = newUser
    }

    // Login using backend API; expects response containing { token, user }
    const loginWithCredentials = async (credentials) => {
        try {
            const api = useApi()
            const resp = await api.post('/auth/login', credentials)
            const data = resp.data || resp
            if (data.token) {
                // store token first so api interceptor can attach it
                login(data.token, null)
                try {
                    const userResp = await api.get('/auth/user')
                    const userData = userResp.data
                    // update stored user
                    login(data.token, userData)
                    return { token: data.token, user: userData }
                } catch (e) {
                    // If fetching user fails, still return token
                    return { token: data.token }
                }
            }
            return data
        } catch (e) {
            throw e
        }
    }

    const logout = () => {
        token.value = null
        user.value = null
        navigateTo('/auth/login')
    }

    // Deprecated: useCookie handles initialization automatically
    const initAuth = () => {
        // No-op
    }

    return {
        token,
        user,
        login,
        loginWithCredentials,
        logout,
        initAuth
    }
}
