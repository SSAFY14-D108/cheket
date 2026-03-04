import AsyncStorage from '@react-native-async-storage/async-storage';

const ACCESS_TOKEN_KEY = 'auth/accessToken';
const REFRESH_TOKEN_KEY = 'auth/refreshToken';

export async function saveTokens(nextAccessToken: string, nextRefreshToken: string): Promise<void> {
  await AsyncStorage.setMany({
    [ACCESS_TOKEN_KEY]: nextAccessToken,
    [REFRESH_TOKEN_KEY]: nextRefreshToken,
  });
}

export async function clearTokens(): Promise<void> {
  await AsyncStorage.removeMany([ACCESS_TOKEN_KEY, REFRESH_TOKEN_KEY]);
}

export async function loadAccessToken(): Promise<string | null> {
  return AsyncStorage.getItem(ACCESS_TOKEN_KEY);
}

export async function loadRefreshToken(): Promise<string | null> {
  return AsyncStorage.getItem(REFRESH_TOKEN_KEY);
}
