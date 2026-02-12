import { useCallback } from 'react';

export const useDialogKeyboardShortcuts = ({ onConfirm, onCancel, onClose } = {}) => {
  const handleKeyDown = useCallback((e) => {
    if (onClose) {
      if (e.key === 'Enter' || e.key === 'Escape') {
        e.preventDefault();
        onClose();
      }
    } else {
      if (e.key === 'Enter' && onConfirm) {
        e.preventDefault();
        onConfirm();
      } else if (e.key === 'Escape' && onCancel) {
        e.preventDefault();
        onCancel();
      }
    }
  }, [onConfirm, onCancel, onClose]);

  return { handleKeyDown };
};
