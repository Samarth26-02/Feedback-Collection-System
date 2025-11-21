import axios from 'axios';

// Your Java backend is running on port 8080
const API_URL = 'http://localhost:8080/api';

// Create axios instance with default config
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error('Error response:', error.response.data);
      return Promise.reject({
        response: {
          data: error.response.data,
          status: error.response.status
        }
      });
    } else if (error.request) {
      // The request was made but no response was received
      console.error('No response received:', error.request);
      return Promise.reject({
        response: {
          data: { message: 'No response from server. Please check your connection.' },
          status: 0
        }
      });
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Request setup error:', error.message);
      return Promise.reject({
        response: {
          data: { message: 'Error setting up request. Please try again.' },
          status: 0
        }
      });
    }
  }
);

// ===================================
// Auth API Calls (Matches your auth.js)
// ===================================

// This will make a POST request to http://localhost:5000/api/auth/signup
export const registerUser = async (userData) => {
  try {
    const response = await api.post('/auth/signup', userData);
    return response;
  } catch (error) {
    console.error('Registration error:', error);
    throw error;
  }
};

// This will make a POST request to http://localhost:8080/api/auth/login
export const loginUser = async (credentials) => {
  try {
    const response = await api.post('/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      // Also store user info for dashboard
      localStorage.setItem('userInfo', JSON.stringify(response.data));
    }
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const logoutUser = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userInfo');
};

export const getCurrentUser = async () => {
  try {
    const response = await api.get('/auth/me');
    return response.data;
  } catch (error) {
    console.error('Get current user error:', error);
    throw error;
  }
};

// We will add more functions here later for forms and feedback
export default api;