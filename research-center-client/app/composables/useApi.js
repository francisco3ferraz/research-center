import axios from 'axios'

export const useApi = () => {
  const config = useRuntimeConfig()
  const token = useState('token', () => null)

  const api = axios.create({
    baseURL: config.public.apiBase,
    // Do not set a default Content-Type here. Let the browser set
    // the appropriate Content-Type (and boundary) for FormData requests.
  })

  api.interceptors.request.use((request) => {
    if (token.value) {
      request.headers = request.headers || {}
      request.headers.Authorization = `Bearer ${token.value}`
    }
    return request
  }, (error) => Promise.reject(error))

  return api
}

export default useApi
