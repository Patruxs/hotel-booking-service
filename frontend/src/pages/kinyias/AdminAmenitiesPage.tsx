'use client';
import { useState } from 'react';
import { useGetAmenitiesQuery } from '@/features/amentites/queries';
import { Amenity } from '@/features/amentites/types';
import * as Icons from 'lucide-react';
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from '@/components/ui/table';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import EllipsisPagination from '@/components/ui/EllipsisPagination';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import { ConfirmDialog } from '@/components/common/CofirmDialog';
import { useDeleteAmenityMutation } from '@/features/amentites/mutations';
export default function AmenitiesPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const pageSize = 10;
  const deleteMutation = useDeleteAmenityMutation();
  // Pass pagination and search params to the query
  const { data, isLoading, isError } = useGetAmenitiesQuery({
    limit: pageSize,
    page,
    q: search || undefined,
    isActive: true,
  });
  const amenities = data?.data ?? [];
  const total = data?.meta?.total ?? 0;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  if (isError)
    return <div className="p-6 text-red-500">Failed to load amenities.</div>;
  return (
    <div className="container mx-auto py-6 space-y-4" style={{display: 'block'}}>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold">Amenities</h1>
        <div className="flex items-center gap-2">
          <Input
            placeholder="Search by label..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            className="w-64"
          />
          <Link to="/admin/amenities/new">
                <Button className="bg-primary hover:bg-primary/90 text-primary-foreground">
                  <Icons.Plus size={20} className="mr-2" />
                  Add Amenity
                </Button>
                </Link>
        </div>
      </div>
      {isLoading? (<div className="p-6">Loading amenities...</div>): (
        <div>
      <Card className="overflow-hidden mb-5">
        <Table className="table-fixed w-full">
          <TableHeader>
            <TableRow className="bg-secondary/50">
              <TableHead className="w-1/6">Icon</TableHead>
              <TableHead className="w-3/6">Label</TableHead>
              <TableHead className="w-2/6">Key</TableHead>
              <TableHead className="w-1/6">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {amenities.map((amenity: Amenity) => {
                const IconComponent = amenity.iconKey ? (Icons as any)[amenity.iconKey] : null;
              return (
                <TableRow key={amenity.id} className="border-b border-border">
                  <TableCell>
                    {IconComponent ? (
                      <IconComponent size={20} />
                    ) : (
                      <span className="text-muted-foreground">—</span>
                    )}
                  </TableCell>
                  <TableCell className="w-3/6">{amenity.label}</TableCell>
                  <TableCell className="w-2/6 text-muted-foreground">
                    {amenity.key}
                  </TableCell>
                  <TableCell className="w-1/6">
                    <Link to={`/admin/amenities/${amenity.id}`}>
                      <Button variant="ghost" size="icon" title="Edit">
                        <Icons.Pencil size={20} />
                      </Button>
                    </Link>
                    <Button
                      variant="ghost"
                      size="icon"
                      title="Delete"
                      className="text-destructive hover:text-destructive"
                      onClick={() => setDeleteId(amenity.id)}
                    >
                      <Icons.Trash2 size={20} />
                    </Button>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>
      </Card>
      <EllipsisPagination
        currentPage={page}
        totalPages={totalPages}
        onPageChange={(newPage) => setPage(newPage)}
      />
        </div>
      )}
      <ConfirmDialog
        open={!!deleteId}
        title="Delete Amenity"
        description="The amenity will be deactivated while existing room references remain intact."
        confirmText="Delete"
        cancelText="Cancel"
        isLoading={deleteMutation.isPending}
        onConfirm={() => {
          if (!deleteId) return;
          deleteMutation.mutate(deleteId, { onSettled: () => setDeleteId(null) });
        }}
        onCancel={() => setDeleteId(null)}
      />
    </div>
  );
}
