import { Link } from "react-router-dom";
import { DoorOpen } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useHotelsQuery } from "@/features/hotels/queries";

export default function AdminRoomsPage() {
  const { data, isLoading } = useHotelsQuery({ page: 1, limit: 100 });
  const hotels = data?.data ?? [];

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Physical Rooms</h1>
        <p className="text-muted-foreground">Choose a hotel to view rooms and update housekeeping condition.</p>
      </div>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        {isLoading ? <p className="text-muted-foreground">Loading hotels...</p> : hotels.map((hotel: any) => (
          <Card key={hotel.id}>
            <CardHeader><CardTitle className="text-lg">{hotel.name}</CardTitle></CardHeader>
            <CardContent>
              <Button asChild className="w-full">
                <Link to={`/admin/rooms/${hotel.id}`}><DoorOpen className="mr-2 h-4 w-4" />View Physical Rooms</Link>
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
      {!isLoading && hotels.length === 0 && <p className="text-muted-foreground">No assigned hotels found.</p>}
    </div>
  );
}
