import {useQuery} from '@tanstack/react-query';

type ShowSummary = {
  id: string;
  title: string;
  startsAt: string;
};

export function useShowList() {
  return useQuery<ShowSummary[]>({
    queryKey: ['shows'],
    queryFn: async () => [],
  });
}
