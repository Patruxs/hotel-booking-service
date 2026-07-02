// @ts-nocheck
import { useMutation, useQueryClient } from "@tanstack/react-query"
import { cancelBooking, cancelMyBooking, checkInBooking, createBooking, createPayment, updateBookingStatus } from "./api"
import { BookingStatus, CreateBookingDto } from "./types"
export const useCreateBookingMutation = (hotelId: string) => {
    return useMutation({
        mutationFn: (payload: CreateBookingDto)=> createBooking(hotelId, payload),
    })
}
export const useUpdateBookingStatusMutation = (hotelId: string, bookingId: string) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: BookingStatus)=> updateBookingStatus(hotelId, bookingId, payload),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: ['bookings', hotelId],
            });
            queryClient.invalidateQueries({
                queryKey: ['booking', hotelId, bookingId],
            });
        },
    })
}
export const useCancelBookingMutation = (hotelId: string, bookingId: string) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: ()=> cancelBooking(hotelId, bookingId),
        onSuccess: () => {
            queryClient.invalidateQueries({
                queryKey: ['bookings', hotelId],
            });
        },
    })
}
export const useCancelMyBookingMutation = (bookingId: string) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: () => cancelMyBooking(bookingId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['my-booking', bookingId] });
            queryClient.invalidateQueries({ queryKey: ['my-bookings'] });
        },
    })
}
export const useCreatePaymentMutation = (bookingId: string) => {
    return useMutation({
        mutationFn: ()=> createPayment(bookingId),
    })
}
export const useCheckInBookingMutation = (hotelId: string, bookingId: string) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (payload: any) => checkInBooking(hotelId, bookingId, payload),
        onSuccess: () => {
             queryClient.invalidateQueries({ queryKey: ['bookings', hotelId] });
             queryClient.invalidateQueries({ queryKey: ['booking', hotelId, bookingId] });
        }
    });
}
