export type BookingDateRange = {
  checkIn: string;
  checkOut: string;
};

const DATE_INPUT_PATTERN = /^\d{4}-\d{2}-\d{2}$/;

export function bookingToday(reference = new Date()): Date {
  return new Date(reference.getFullYear(), reference.getMonth(), reference.getDate());
}

export function formatBookingDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function parseBookingDate(value: string | null | undefined): Date | null {
  if (!value || !DATE_INPUT_PATTERN.test(value)) return null;

  const [year, month, day] = value.split("-").map(Number);
  const parsed = new Date(year, month - 1, day);
  return formatBookingDate(parsed) === value ? parsed : null;
}

export function addBookingDays(value: string, days: number): string {
  const parsed = parseBookingDate(value);
  if (!parsed) throw new Error(`Invalid booking date: ${value}`);
  parsed.setDate(parsed.getDate() + days);
  return formatBookingDate(parsed);
}

export function isValidBookingDateRange(
  checkIn: string | null | undefined,
  checkOut: string | null | undefined,
  reference = new Date(),
): boolean {
  const parsedCheckIn = parseBookingDate(checkIn);
  const parsedCheckOut = parseBookingDate(checkOut);

  return Boolean(
    parsedCheckIn
      && parsedCheckOut
      && parsedCheckIn >= bookingToday(reference)
      && parsedCheckOut > parsedCheckIn,
  );
}

export function normalizeBookingDateRange(
  checkIn: string | null | undefined,
  checkOut: string | null | undefined,
  reference = new Date(),
): BookingDateRange {
  const today = bookingToday(reference);
  const requestedCheckIn = parseBookingDate(checkIn);
  const normalizedCheckIn = requestedCheckIn && requestedCheckIn >= today
    ? requestedCheckIn
    : today;
  const requestedCheckOut = parseBookingDate(checkOut);
  const normalizedCheckOut = requestedCheckOut && requestedCheckOut > normalizedCheckIn
    ? requestedCheckOut
    : new Date(normalizedCheckIn.getFullYear(), normalizedCheckIn.getMonth(), normalizedCheckIn.getDate() + 1);

  return {
    checkIn: formatBookingDate(normalizedCheckIn),
    checkOut: formatBookingDate(normalizedCheckOut),
  };
}
