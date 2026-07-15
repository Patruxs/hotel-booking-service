// @ts-nocheck
import { z } from 'zod';
import { isBefore, startOfDay } from 'date-fns';
import { BannerLinkType } from './types';

const startAtIsTodayOrLater = (startAt?: string) => {
  if (!startAt) return true;
  return !isBefore(startOfDay(new Date(startAt)), startOfDay(new Date()));
};

export const createBannerSchema = z.object({
  title: z.string().optional(),
  subtitle: z.string().optional(),
  images: z.array(z.string().url('Invalid image URL')).min(1, 'At least one image is required'),
  link: z.string().optional(),
  linkType: z.nativeEnum(BannerLinkType).optional(),
  position: z.number().int().min(0).optional(),
  isActive: z.boolean().optional(),
  startAt: z.string().datetime().optional().or(z.literal('')),
  endAt: z.string().datetime().optional().or(z.literal('')),
}).refine(
  (data) => {
    if (data.startAt && data.endAt) {
      return new Date(data.startAt) <= new Date(data.endAt);
    }
    return true;
  },
  {
    message: 'Start date must be before or equal to end date',
    path: ['endAt'],
  }
).refine(
  (data) => startAtIsTodayOrLater(data.startAt),
  {
    message: 'Start date cannot be before today',
    path: ['startAt'],
  }
);
export const updateBannerSchema = z.object({
  title: z.string().optional(),
  subtitle: z.string().optional(),
  images: z.array(z.string().url('Invalid image URL')).min(1, 'At least one image is required').optional(),
  link: z.string().optional(),
  linkType: z.nativeEnum(BannerLinkType).optional(),
  position: z.number().int().min(0).optional(),
  isActive: z.boolean().optional(),
  startAt: z.string().datetime().optional().or(z.literal('')),
  endAt: z.string().datetime().optional().or(z.literal('')),
}).refine(
  (data) => {
    if (data.startAt && data.endAt) {
      return new Date(data.startAt) <= new Date(data.endAt);
    }
    return true;
  },
  {
    message: 'Start date must be before or equal to end date',
    path: ['endAt'],
  }
);
export type CreateBannerFormData = z.infer<typeof createBannerSchema>;
export type UpdateBannerFormData = z.infer<typeof updateBannerSchema>;
