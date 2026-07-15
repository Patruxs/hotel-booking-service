'use client';
import { useState, useMemo } from 'react';
import { formatCurrency } from '@/utils/currency';
import { useRevenueChartQuery } from '@/features/dashboard/queries';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { format, isValid, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';
import { CalendarIcon } from 'lucide-react';
export type RevenueGroupBy = 'day' | 'week' | 'month';
const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];
interface RevenueChartProps {
    hotelId?: string;
}

interface RevenuePoint {
  period?: string | null;
  month?: string | null;
  date?: string | null;
  revenue: number;
}

function formatRevenuePeriod(value: unknown, groupBy: RevenueGroupBy) {
  if (typeof value !== 'string') return null;

  const label = value.trim();
  if (!label) return null;

  if (/^\d{4}-\d{2}-\d{2}(?:$|T)/.test(label)) {
    const date = parseISO(label);
    if (isValid(date)) {
      return format(date, groupBy === 'month' ? 'MM/yyyy' : 'dd/MM/yyyy', { locale: vi });
    }
  }

  return label;
}

export function RevenueChart({ hotelId }: RevenueChartProps) {
  const currentYear = new Date().getFullYear();
  const [viewType, setViewType] = useState<'year' | 'range'>('year');
  const [selectedYear, setSelectedYear] = useState(currentYear);
  const [dateRange, setDateRange] = useState<{
    from: Date | undefined;
    to: Date | undefined;
  }>({
    from: new Date(currentYear, 0, 1),
    to: new Date(currentYear, 11, 31),
  });
  const [groupBy, setGroupBy] = useState<RevenueGroupBy>('month');
  const queryParams = useMemo(() => {
     if (viewType === 'year') {
         return {
             hotelId,
             groupBy: 'month' as const,
             year: selectedYear
         }
     }
     return {
        hotelId,
        groupBy: groupBy === 'month' ? 'month' : 'day' as any,
        from: dateRange.from?.toISOString(),
        to: dateRange.to?.toISOString(),
     }
  }, [hotelId, viewType, selectedYear, dateRange, groupBy]);
  const { data: rawData, isLoading } = useRevenueChartQuery(queryParams as any);
  const chartData = useMemo(() => {
      if (!rawData) return [];
      if (Array.isArray(rawData) && typeof rawData[0] === 'number') {
        return (rawData as number[]).map((total, index) => ({
            name: monthNames[index],
            revenue: total,
        }));
      }
        if (!Array.isArray(rawData)) return [];

        const list = rawData as RevenuePoint[];
        return list.flatMap((item) => {
          if (!item || typeof item !== 'object') return [];

          const point = item as RevenuePoint;
          const name = formatRevenuePeriod(point.period ?? point.month ?? point.date, groupBy);
          return name ? [{ name, revenue: point.revenue }] : [];
        });
  }, [rawData, groupBy]);
  return (
    <>
      <Card>
        <CardHeader>
          <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
            <CardTitle>Revenue</CardTitle>
            <div className="flex flex-wrap items-center gap-2">
              {}
              <Select
                value={viewType}
                onValueChange={(value: 'year' | 'range') => setViewType(value)}
              >
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="View type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="year">Year</SelectItem>
                  <SelectItem value="range">Range</SelectItem>
                </SelectContent>
              </Select>
              {viewType === 'year' ? (
                <Select
                  value={selectedYear.toString()}
                  onValueChange={(value) => setSelectedYear(parseInt(value))}
                >
                  <SelectTrigger className="w-[120px]">
                    <SelectValue placeholder="Select year" />
                  </SelectTrigger>
                  <SelectContent>
                    {Array.from({ length: 5 }, (_, i) => currentYear - i).map(
                      (year) => (
                        <SelectItem key={year} value={year.toString()}>
                          {year}
                        </SelectItem>
                      )
                    )}
                  </SelectContent>
                </Select>
              ) : (
                <>
                  {}
                  <Popover>
                    <PopoverTrigger asChild>
                      <Button
                        variant="outline"
                        className="w-[280px] justify-start text-left font-normal"
                      >
                        <CalendarIcon className="mr-2 h-4 w-4" />
                        {dateRange?.from ? (
                          dateRange.to ? (
                            <>
                              {format(dateRange.from, 'dd/MM/yyyy')} -{' '}
                              {format(dateRange.to, 'dd/MM/yyyy')}
                            </>
                          ) : (
                            format(dateRange.from, 'dd/MM/yyyy')
                          )
                        ) : (
                          <span>Pick a date range</span>
                        )}
                      </Button>
                    </PopoverTrigger>
                    <PopoverContent className="w-auto p-0" align="start">
                      <Calendar
                        initialFocus
                        mode="range"
                        defaultMonth={dateRange?.from}
                        selected={{
                          from: dateRange?.from,
                          to: dateRange?.to,
                        }}
                        onSelect={(range) => {
                          setDateRange({
                            from: range?.from,
                            to: range?.to,
                          });
                        }}
                        numberOfMonths={2}
                      />
                    </PopoverContent>
                  </Popover>
                  {}
                  <Select
                    value={groupBy}
                    onValueChange={(value: RevenueGroupBy) => setGroupBy(value)}
                  >
                    <SelectTrigger className="w-[120px]">
                      <SelectValue placeholder="By group" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="day">Day</SelectItem>
                      <SelectItem value="week">Week</SelectItem>
                      <SelectItem value="month">Month</SelectItem>
                    </SelectContent>
                  </Select>
                </>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="h-[400px] w-full">
            {isLoading ? (
                <div className="flex h-full items-center justify-center">Loading...</div>
            ) : (
             <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis
                    dataKey="name"
                    tickLine={false}
                    axisLine={false}
                    fontSize={12}
                    tickMargin={8}
                    interval={0}
                    angle={viewType === 'range' ? -45 : 0}
                    textAnchor={viewType === 'range' ? 'end' : 'middle'}
                    height={60}
                />
                <YAxis
                    tickLine={false}
                    axisLine={false}
                    fontSize={12}
                    tickMargin={8}
                    tickFormatter={(value) => `${formatCurrency(Number(value))}`}
                />
                <Tooltip
                    cursor={{ fill: 'rgba(0, 0, 0, 0.05)' }}
                    formatter={(value) => [
                    `${formatCurrency(Number(value))}`,
                    'Revenue',
                    ]}
                />
                <Legend />
                <Bar
                    name="Revenue"
                    dataKey="revenue"
                    fill="currentColor"
                    radius={[4, 4, 0, 0]}
                    className="fill-primary"
                    barSize={30}
                />
                </BarChart>
            </ResponsiveContainer>
           )}
          </div>
        </CardContent>
      </Card>
    </>
  );
}
