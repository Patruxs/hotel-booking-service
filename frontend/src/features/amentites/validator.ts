// @ts-nocheck
import z from "zod";
export const amenityFormSchema = z.object({
    label: z.string().min(3, "Label must be at least 3 characters long"),
    key: z.string().optional(),
    iconKey: z.string().min(1, "Icon is required"),
});
export type AmenityFormSchema = z.infer<typeof amenityFormSchema>;
