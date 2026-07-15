// @ts-nocheck
import { useQuery } from '@tanstack/react-query';
import { getHotelById, getManageHotelById, getHotelMembers, getHotelMemberCandidates, getHotels, getPublicHotels } from './api';
import { HotelsQueryParams } from './types';
export const useHotelsQuery = (params?: HotelsQueryParams) => {
  return useQuery({
    queryKey: ['hotels', params],
    queryFn: () => getHotels(params),
  });
};
export const useHotelDetailQuery = (hotelId: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['hotel', hotelId],
    queryFn: () => getHotelById(hotelId),
    enabled,
  });
};
export const useManageHotelDetailQuery = (hotelId: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['manage-hotel', hotelId],
    queryFn: () => getManageHotelById(hotelId),
    enabled,
  });
};
export const usePublicHotelsQuery = (params?: HotelsQueryParams) => {
  return useQuery({
    queryKey: ['public-hotels', params],
    queryFn: () => getPublicHotels(params),
  });
};
export const useHotelMembersQuery = (hotelId: string, enabled: boolean = true) => {
  return useQuery({
    queryKey: ['hotel-members', hotelId],
    queryFn: () => getHotelMembers(hotelId),
    enabled,
  });
};
export const useHotelMemberCandidatesQuery = (hotelId: string, q = '', enabled: boolean = true) => {
  return useQuery({
    queryKey: ['hotel-member-candidates', hotelId, q],
    queryFn: () => getHotelMemberCandidates(hotelId, q),
    enabled: enabled && !!hotelId,
  });
};
