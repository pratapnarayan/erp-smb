// Use environment variable in production, fallback to /api for local development
// Set VITE_API_BASE_URL in Vercel environment variables for production backend URL
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
