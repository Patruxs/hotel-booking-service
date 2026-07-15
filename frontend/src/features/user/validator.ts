// @ts-nocheck
import { z } from "zod";
export const userFormSchema = z.object({
  firstName: z.string().min(1, "First name is required"),
  lastName: z.string().min(1, "Last name is required"),
  phone: z
    .string()
    .regex(/^\d{10,12}$/, "Phone number must be between 10 and 12 digits"),
  dob: z.string().min(1, "Date of birth is required"),
});
export const adminUserFormSchema = z.object({
  firstName: z.string().min(1, "First name is required"),
  lastName: z.string().min(1, "Last name is required"),
  phone: z
    .string()
    .regex(/^\d{10,12}$/, "Phone number must be between 10 and 12 digits")
    .optional()
    .or(z.literal("")),
  dob: z.string().optional().or(z.literal("")),
});
export const changePasswordFormSchema = z
  .object({
    currentPassword: z.string().min(1, "Current password is required"),
    newPassword: z.string().min(1, "New password is required"),
    confirmPassword: z.string().min(1, "Confirm password is required"),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });
export const roleAssignUserFormSchema = z.object({
  roleIds: z.array(z.string()).min(1, "At least one role is required"),
});
export type ChangePasswordFormValues = z.infer<typeof changePasswordFormSchema>;
export type UserFormValues = z.infer<typeof userFormSchema>;
export type AdminUserFormValues = z.infer<typeof adminUserFormSchema>;
export type RoleAssignUserFormValues = z.infer<typeof roleAssignUserFormSchema>;
