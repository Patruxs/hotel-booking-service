'use client';
import { useState } from 'react';
import { useReviewEligibilityQuery, useReviewsQuery } from '../queries';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { formatDistanceToNow } from 'date-fns';
import { MessageSquarePlus, Star } from 'lucide-react';
import { AppImage as Image } from '@/components/AppImage';
import EllipsisPagination from '@/components/ui/EllipsisPagination';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/providers/AuthProvider';
import CreateReviewDialog from './CreateReviewDialog';
interface ReviewListProps {
  hotelId: string;
}
export default function ReviewList({ hotelId }: ReviewListProps) {
  const [page, setPage] = useState(1);
  const [reviewDialogOpen, setReviewDialogOpen] = useState(false);
  const limit = 5;
  const { user } = useAuth();
  const isCustomer = user?.roles?.some((role) => role.name === 'CUSTOMER') ?? false;
  const { data: reviewsResponse, isLoading } = useReviewsQuery(hotelId, { page, limit });
  const { data: eligibility } = useReviewEligibilityQuery(hotelId, isCustomer);
  const reviews = reviewsResponse?.items || [];
  const meta = reviewsResponse?.total || 0;
  const totalPages = Math.max(1, Math.ceil(meta / limit));
  const canReview = Boolean(eligibility?.canReview && eligibility?.bookingId);
  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <h3 className="text-2xl font-bold">Guest Reviews ({meta})</h3>
        {canReview && (
          <Button type="button" onClick={() => setReviewDialogOpen(true)}>
            <MessageSquarePlus className="mr-2 h-4 w-4" />
            Write a Review
          </Button>
        )}
      </div>
      {isLoading ? (
        <div className="text-gray-500">Loading reviews...</div>
      ) : reviews.length === 0 ? (
        <div className="text-gray-500 italic">No reviews yet for this hotel.</div>
      ) : (
        <div className="space-y-6">
          {reviews.map((review: any) => (
            <div key={review.id} className="border-b border-gray-100 pb-6 last:border-0 last:pb-0">
              <div className="flex items-start gap-4">
               <Avatar className="w-10 h-10 border border-gray-200">
                  <AvatarImage src={review.user?.avatar?.secureUrl} />
                  <AvatarFallback>{(review.user?.firstName?.[0] || 'G')}{(review.user?.lastName?.[0] || '')}</AvatarFallback>
               </Avatar>
               <div className="flex-1">
                  <div className="flex items-center justify-between mb-1">
                     <span className="font-semibold text-gray-900">
                        {review.user?.firstName} {review.user?.lastName}
                     </span>
                     <span className="text-sm text-gray-500">
                        {review.createdAt ? formatDistanceToNow(new Date(review.createdAt), { addSuffix: true }) : 'Recently'}
                     </span>
                  </div>
                  <div className="flex items-center gap-1 mb-2">
                     {Array.from({ length: 5 }).map((_, i) => (
                          <Star
                             key={i}
                             className={`w-4 h-4 ${i < review.rating ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}`}
                          />
                       ))}
                    </div>
                    {review.title && <h4 className="font-medium text-gray-900 mb-1">{review.title}</h4>}
                    {review.content && <p className="text-gray-600 leading-relaxed mb-3">{review.content}</p>}
                  {review.images && review.images.length > 0 && (
                      <div className="flex gap-2 overflow-x-auto pb-2">
                          {review.images.map((img: any) => (
                            <Dialog key={img.id}>
                                <DialogTrigger asChild>
                                    <div className="relative w-24 h-24 flex-shrink-0 cursor-pointer overflow-hidden rounded-md border hover:opacity-90 transition-opacity">
                                        <Image
                                            src={img.url}
                                            alt="Review image"
                                            fill
                                            className="object-cover"
                                        />
                                    </div>
                                </DialogTrigger>
                                <DialogContent className="max-w-[90vw] lg:max-w-4xl h-auto border-none p-0 shadow-none">
                                  <DialogHeader className="sr-only">
                                    <DialogTitle>Review Image</DialogTitle>
                                  </DialogHeader>
                                  <div className="relative aspect-[4/3] w-full overflow-hidden rounded-2xl bg-black/5 shadow-2xl backdrop-blur-sm md:aspect-video">
                                    <Image
                                      src={img.url}
                                      alt="Review image"
                                      fill
                                      className="object-contain transition-all duration-500 hover:scale-[1.02]"
                                      priority
                                    />
                                  </div>
                                </DialogContent>
                            </Dialog>
                            ))}
                        </div>
                    )}
                 </div>
              </div>
            </div>
          ))}
        </div>
      )}
      {totalPages > 1 && (
        <div className="flex justify-center pt-4">
          <EllipsisPagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </div>
      )}
      {canReview && (
        <CreateReviewDialog
          key={eligibility.bookingId}
          open={reviewDialogOpen}
          onOpenChange={setReviewDialogOpen}
          bookingId={eligibility.bookingId}
          hotelId={hotelId}
        />
      )}
    </div>
  );
}
