export const environment = {
    production: false,
    backendBaseUrl: 
        (window as any).env?.VITE_GROUP123_BACKEND_BASE_URL ||
        (window as any).env?.VITE_BANKING_API_BASE_URL ||
        (window as any).env?.VITE_LOGIN_API_BASE_URL ||
        (window as any).env?.VITE_ACCOUNT_SERVICE_BASE_URL ||
        ''
};
