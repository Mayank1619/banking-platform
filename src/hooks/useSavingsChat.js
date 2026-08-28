import { useMutation } from '@tanstack/react-query';
import { askSavingsInsight } from '../api/chat';

export function useSavingsChat() {
  return useMutation({
    mutationFn: askSavingsInsight,
    throwOnError: false
  });
}
