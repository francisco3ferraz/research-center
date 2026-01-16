export const useAuth = () => {
    const token = useState('token', () => null)
    const user = useState('user', () => null)

    const login = (newToken, newUser) => {
        token.value = newToken
        user.value = newUser
        // Basic persistence for simpler functional testing (optional, can be improved with cookies)
        if (process.client) {
            localStorage.setItem('auth_token', newToken)
            localStorage.setItem('auth_user', JSON.stringify(newUser))
        }
    }

    const logout = () => {
        token.value = null
        user.value = null
        if (process.client) {
            localStorage.removeItem('auth_token')
            localStorage.removeItem('auth_user')
        }
        navigateTo('/auth/login')
    }

    const initAuth = () => {
        if (process.client && !token.value) {
            const storedToken = localStorage.getItem('auth_token')
            const storedUser = localStorage.getItem('auth_user')
            if (storedToken) {
                token.value = storedToken
            }
            if (storedUser) {
                try {
                    user.value = JSON.parse(storedUser)
                } catch (e) {
                    console.error('Invalid stored user', e)
                }
            }
        }
    }

    return {
        token,
        user,
        login,
        logout,
        initAuth
    }
}
