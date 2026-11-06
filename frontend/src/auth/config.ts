const api = process.env.EXPO_PUBLIC_API_URL || '';
export const config = { api: api.endsWith('/') ? api.slice(0,-1) : api, domain: process.env.EXPO_PUBLIC_ALLOWED_DOMAIN || 'hbplus.fit', web: process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID || '', android: process.env.EXPO_PUBLIC_GOOGLE_ANDROID_CLIENT_ID || '', ios: process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID || '' };
