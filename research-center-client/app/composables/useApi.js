export const useApi = () => {
  const config = useRuntimeConfig()
  // Use useAuth to get the shared token state
  const { token } = useAuth()

  // Hardcoded to avoid '8o8o' typo ghost issue
  const baseURL = 'http://localhost:8080/research-center/api'

  // Customized $fetch wrapper
  const api = $fetch.create({
    baseURL: baseURL,
    onRequest({ request, options }) {
      if (token.value) {
        options.headers = options.headers || {}

        // Handle both plain object and Headers object
        if (options.headers instanceof Headers) {
          options.headers.set('Authorization', `Bearer ${token.value}`)
        } else if (Array.isArray(options.headers)) {
          options.headers.push(['Authorization', `Bearer ${token.value}`])
        } else {
          options.headers.Authorization = `Bearer ${token.value}`
        }
      }
    },
    onResponseError({ response }) {
      // Optional: Handle 401 Unauthorized globally
      if (response.status === 401) {
        // Only redirect if we are on client-side to avoid loops/issues
        if (process.client) {
          if (logout) logout() // ensure logout logic exists
          else navigateTo('/auth/login')
        }
      }
      return Promise.reject(response)
    }
  })

  // Adapter to make it compatible with axios-style calls used in pages (optional but helpful)
  // or we can just expose methods.
  // For now, let's keep it simple and expose HTTP methods that behave like axios (return data property? no, $fetch returns body directly)
  // IMPORTANT: $fetch returns the parsed JSON body directly, while axios returns { data: body, ... }

  // To minimize refactoring in pages, we can return an object that mimics axios response structure
  // OR preferrably, we update the calling code to expect direct data. 
  // Given the task is "like the classes", classes usually use $fetch directly.
  // However, to keep existing code working, I will add wrappers.

  return {
    get: async (url, opts) => {
      const data = await api(url, { ...opts, method: 'GET' })
      return { data } // mimic axios struct
    },
    post: async (url, body, opts) => {
      const data = await api(url, { ...opts, method: 'POST', body })
      return { data }
    },
    put: async (url, body, opts) => {
      const data = await api(url, { ...opts, method: 'PUT', body })
      return { data }
    },
    patch: async (url, body, opts) => {
      const data = await api(url, { ...opts, method: 'PATCH', body })
      return { data }
    },
    delete: async (url, opts) => {
      const data = await api(url, { ...opts, method: 'DELETE' })
      return { data }
    }
  }
}

export default useApi
