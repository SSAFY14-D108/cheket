import {create} from 'zustand';

type UiState = {
  toastMessage: string | null;
  modalOpen: boolean;
  showToast: (message: string) => void;
  clearToast: () => void;
  openModal: () => void;
  closeModal: () => void;
};

export const useUiStore = create<UiState>(set => ({
  toastMessage: null,
  modalOpen: false,
  showToast: message => set({toastMessage: message}),
  clearToast: () => set({toastMessage: null}),
  openModal: () => set({modalOpen: true}),
  closeModal: () => set({modalOpen: false}),
}));
