// @ts-nocheck
export interface Amenity {
    id: string;
    key: string;
    iconKey: string | null;
  label: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}
export interface AmenitiesQueryParams {
  page?:number;
  limit?: number;
  offset?: number;
  q?: string;
  isActive?: boolean;
}
