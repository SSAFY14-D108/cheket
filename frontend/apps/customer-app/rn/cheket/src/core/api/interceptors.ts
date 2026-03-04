import {AxiosError, AxiosInstance, InternalAxiosRequestConfig} from 'axios';
import {loadAccessToken} from '../storage/secureStore';
import {parseUnknownError} from './errors';

export function attachInterceptors(instance: AxiosInstance): void {
  instance.interceptors.request.use(withAuthHeader);
  instance.interceptors.response.use(
    response => response,
    onResponseError,
  );
}

async function withAuthHeader(
  config: InternalAxiosRequestConfig,
): Promise<InternalAxiosRequestConfig> {
  const token = await loadAccessToken();

  if (!token) {
    return config;
  }

  config.headers.Authorization = `Bearer ${token}`;
  return config;
}

function onResponseError(error: AxiosError): Promise<never> {
  return Promise.reject(parseUnknownError(error));
}
