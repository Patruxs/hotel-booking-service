import { useMemo, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Loader2 } from "lucide-react";
import toast from "react-hot-toast";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useCreateBookingMutation } from "../mutations";
import { useQueryPublicRoomTypes } from "@/features/room-types/queries";
import { getApiErrorMessage } from "@/lib/apiErrors";
import {
  addBookingDays,
  formatBookingDate,
  isValidBookingDateRange,
  normalizeBookingDateRange,
  parseBookingDate,
} from "../bookingDateRules";

interface CreateBookingDialogProps {
  hotelId: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const initialForm = () => ({
  ...normalizeBookingDateRange(null, null),
  guestName: "",
  guestEmail: "",
  guestPhone: "",
  note: "",
  roomTypeId: "",
  quantity: 1,
});

export function CreateBookingDialog({ hotelId, open, onOpenChange }: CreateBookingDialogProps) {
  const [form, setForm] = useState(initialForm);
  const queryClient = useQueryClient();
  const createMutation = useCreateBookingMutation(hotelId);
  const { data: roomTypesData, isLoading: roomTypesLoading } = useQueryPublicRoomTypes(hotelId, open);
  const roomTypes = useMemo(() => roomTypesData?.data ?? [], [roomTypesData]);
  const today = formatBookingDate(new Date());
  const minimumCheckOut = parseBookingDate(form.checkIn)
    ? addBookingDays(form.checkIn, 1)
    : today;

  const updateField = (field: string, value: string | number) => {
    setForm((current) => ({ ...current, [field]: value }));
  };

  const updateCheckIn = (checkIn: string) => {
    setForm((current) => ({
      ...current,
      checkIn,
      checkOut: current.checkOut > checkIn ? current.checkOut : addBookingDays(checkIn, 1),
    }));
  };

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen && !createMutation.isPending) {
      setForm(initialForm());
    }
    onOpenChange(nextOpen);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!form.roomTypeId) {
      toast.error("Select a room type");
      return;
    }
    if (!isValidBookingDateRange(form.checkIn, form.checkOut)) {
      toast.error("Check-in must be today or later, and check-out must be at least one day later");
      return;
    }

    try {
      await createMutation.mutateAsync({
        hotelId,
        checkIn: form.checkIn,
        checkOut: form.checkOut,
        guestName: form.guestName.trim(),
        guestEmail: form.guestEmail.trim(),
        guestPhone: form.guestPhone.trim(),
        note: form.note.trim(),
        items: [{ roomTypeId: form.roomTypeId, quantity: form.quantity }],
      });
      await queryClient.invalidateQueries({ queryKey: ["bookings", hotelId] });
      toast.success("Guest booking created");
      handleOpenChange(false);
    } catch (error) {
      toast.error(getApiErrorMessage(error, "Failed to create guest booking"));
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Create Booking</DialogTitle>
          <DialogDescription>Create a reservation on behalf of a guest.</DialogDescription>
        </DialogHeader>
        <form className="grid gap-4" onSubmit={handleSubmit}>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Check-in" htmlFor="reception-check-in">
              <Input id="reception-check-in" type="date" min={today} value={form.checkIn} onChange={(event) => updateCheckIn(event.target.value)} required />
            </Field>
            <Field label="Check-out" htmlFor="reception-check-out">
              <Input id="reception-check-out" type="date" min={minimumCheckOut} value={form.checkOut} onChange={(event) => updateField("checkOut", event.target.value)} required />
              </Field>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Guest name" htmlFor="reception-guest-name">
              <Input id="reception-guest-name" value={form.guestName} onChange={(event) => updateField("guestName", event.target.value)} required />
            </Field>
            <Field label="Guest email" htmlFor="reception-guest-email">
              <Input id="reception-guest-email" type="email" value={form.guestEmail} onChange={(event) => updateField("guestEmail", event.target.value)} required />
            </Field>
          </div>
          <Field label="Guest phone" htmlFor="reception-guest-phone">
            <Input id="reception-guest-phone" value={form.guestPhone} onChange={(event) => updateField("guestPhone", event.target.value)} required />
          </Field>
          <div className="grid gap-4 sm:grid-cols-[1fr_8rem]">
            <div className="grid gap-2">
              <Label htmlFor="reception-room-type">Room type</Label>
              <Select value={form.roomTypeId} onValueChange={(value) => updateField("roomTypeId", value)} disabled={roomTypesLoading}>
                <SelectTrigger id="reception-room-type">
                  <SelectValue placeholder={roomTypesLoading ? "Loading room types..." : "Select room type"} />
                </SelectTrigger>
                <SelectContent>
                  {roomTypes.map((roomType: any) => (
                    <SelectItem key={roomType.id} value={roomType.id}>{roomType.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <Field label="Quantity" htmlFor="reception-room-quantity">
              <Input id="reception-room-quantity" type="number" min={1} value={form.quantity} onChange={(event) => updateField("quantity", Math.max(1, Number(event.target.value)))} required />
            </Field>
          </div>
          <Field label="Note" htmlFor="reception-booking-note">
            <Textarea id="reception-booking-note" value={form.note} onChange={(event) => updateField("note", event.target.value)} />
          </Field>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => handleOpenChange(false)} disabled={createMutation.isPending}>Cancel</Button>
            <Button type="submit" disabled={createMutation.isPending || roomTypesLoading}>
              {createMutation.isPending && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Create Booking
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function Field({ label, htmlFor, children }: { label: string; htmlFor: string; children: React.ReactNode }) {
  return <div className="grid gap-2"><Label htmlFor={htmlFor}>{label}</Label>{children}</div>;
}
