let initialized = false;

export function initApp(): void {
  if (initialized) {
    return;
  }

  initialized = true;
}
