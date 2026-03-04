import axios from 'axios';
import {ENV} from '../config/env';
import {attachInterceptors} from './interceptors';

const instance = axios.create({
  baseURL: ENV.BASE_URL,
  timeout: 10000,
});

attachInterceptors(instance);

export const httpClient = {
  get: async <T>(path: string, headers?: Record<string, string>): Promise<T> => {
    const {data} = await instance.get<T>(path, {headers});
    return data;
  },
  post: async <T>(path: string, body?: unknown, headers?: Record<string, string>): Promise<T> => {
    const {data} = await instance.post<T>(path, body, {headers});
    return data;
  },
};
