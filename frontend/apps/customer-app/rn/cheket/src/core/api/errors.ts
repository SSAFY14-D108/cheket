import axios from 'axios';

export type AppErrorCode = 'UNKNOWN' | 'NETWORK' | 'UNAUTHORIZED' | 'HTTP';

export class AppError extends Error {
  code: AppErrorCode;

  constructor(message: string, code: AppErrorCode = 'UNKNOWN') {
    super(message);
    this.name = 'AppError';
    this.code = code;
  }
}

export function parseUnknownError(error: unknown): AppError {
  if (error instanceof AppError) {
    return error;
  }

  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return new AppError('Network error', 'NETWORK');
    }

    if (error.response.status === 401) {
      return new AppError('Unauthorized', 'UNAUTHORIZED');
    }

    return new AppError(`HTTP ${error.response.status}`, 'HTTP');
  }

  if (error instanceof Error) {
    return new AppError(error.message, 'UNKNOWN');
  }

  return new AppError('Unexpected error', 'UNKNOWN');
}
