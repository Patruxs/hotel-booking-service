// @ts-nocheck
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  BulkSetInventoryFormValues,
  UpdateInventoryFormValues,
} from "./validator";
import { createInventory, deleteInventory, updateInventory } from "./api";
export const useCreateInventoryMutation = (hotelId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: BulkSetInventoryFormValues) =>
      createInventory(hotelId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory", hotelId] });
    },
  });
};
export const useUpdateInventoryMutation = (hotelId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: UpdateInventoryFormValues & {
        roomTypeId?: string;
        date?: string;
      };
    }) => updateInventory(hotelId, id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory", hotelId] });
    },
  });
};
export const useDeleteInventoryMutation = (hotelId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ roomTypeId, id }: { roomTypeId: string; id: string }) =>
      deleteInventory(hotelId, roomTypeId, id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory", hotelId] });
    },
  });
};
