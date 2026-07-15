import { useState } from "react";
import { useParams } from "@/hooks/navigation";
import toast from "react-hot-toast";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useHotelDetailQuery } from "@/features/hotels/queries";
import { useQueryRooms } from "@/features/rooms/queries";
import { useMutationUpdateRoomCondition } from "@/features/rooms/mutations";
import { getApiErrorMessage } from "@/lib/apiErrors";

const CONDITIONS = ["CLEAN", "DIRTY", "MAINTENANCE"] as const;

export default function AdminHotelRoomsPage() {
  const { hotelId = "" } = useParams();
  const { data: hotel } = useHotelDetailQuery(hotelId, Boolean(hotelId));
  const { data: roomsData, isLoading } = useQueryRooms(hotelId, undefined, Boolean(hotelId));
  const rooms = roomsData?.data ?? [];
  const updateCondition = useMutationUpdateRoomCondition(hotelId);
  const [updatingRoomId, setUpdatingRoomId] = useState<string | null>(null);

  const handleConditionChange = async (roomId: string, condition: string) => {
    setUpdatingRoomId(roomId);
    try {
      await updateCondition.mutateAsync({ roomId, condition });
      toast.success("Room condition updated");
    } catch (error) {
      toast.error(getApiErrorMessage(error, "Failed to update room condition"));
    } finally {
      setUpdatingRoomId(null);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Physical Rooms</h1>
        <p className="text-muted-foreground">{hotel?.name ?? "Assigned hotel"}</p>
      </div>
      <Card>
        <CardHeader><CardTitle>Rooms</CardTitle></CardHeader>
        <CardContent>
          <Table>
            <TableHeader><TableRow><TableHead>Room</TableHead><TableHead>Condition</TableHead><TableHead className="w-64">Update condition</TableHead></TableRow></TableHeader>
            <TableBody>
              {rooms.map((room: any) => (
                <TableRow key={room.id}>
                  <TableCell className="font-medium">{room.code}</TableCell>
                  <TableCell><Badge variant={room.cleanStatus === "CLEAN" ? "default" : room.cleanStatus === "DIRTY" ? "destructive" : "secondary"}>{room.cleanStatus}</Badge></TableCell>
                  <TableCell>
                    <Select value={room.cleanStatus} onValueChange={(condition) => void handleConditionChange(room.id, condition)} disabled={updatingRoomId === room.id}>
                      <SelectTrigger aria-label={`Condition for room ${room.code}`}><SelectValue /></SelectTrigger>
                      <SelectContent>{CONDITIONS.map((condition) => <SelectItem key={condition} value={condition}>{condition}</SelectItem>)}</SelectContent>
                    </Select>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {isLoading && <p className="py-8 text-center text-muted-foreground">Loading rooms...</p>}
          {!isLoading && rooms.length === 0 && <p className="py-8 text-center text-muted-foreground">No physical rooms found.</p>}
        </CardContent>
      </Card>
    </div>
  );
}
