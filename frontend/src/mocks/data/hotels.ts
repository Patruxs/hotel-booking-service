import type { Hotel, RoomType } from "@/lib/types";

export const hotels: Hotel[] = [
  {
    id: "hanoi-lotus",
    name: "Hanoi Lotus Grand",
    city: "Ha Noi",
    address: "21 Hang Trong, Hoan Kiem",
    rating: 4.8,
    priceFrom: 1250000,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660127/hotel_assets/cckhs1da5q8xc2g5crze.jpg",
    description:
      "Old-quarter hotel with breakfast service, airport transfer, and flexible cancellation policies.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "danang-bay",
    name: "Da Nang Bay Retreat",
    city: "Da Nang",
    address: "Vo Nguyen Giap, Son Tra",
    rating: 4.7,
    priceFrom: 1680000,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660130/hotel_assets/ew9wwe7ag3ujuevelb9p.jpg",
    description:
      "Beachfront rooms, family suites, and package promotions for longer resort stays.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "saigon-central",
    name: "Saigon Central Suites",
    city: "Ho Chi Minh City",
    address: "12 Nguyen Hue, District 1",
    rating: 4.6,
    priceFrom: 1450000,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660132/hotel_assets/cl9w83a0bpnuiisqldgx.jpg",
    description:
      "Business-focused city hotel with meeting rooms, quick check-in, and central access.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
  {
    id: "mock-hotel-4",
    name: "Azure Sands Resort",
    city: "Nha Trang",
    address: "40 Ocean Drive",
    rating: 4.2,
    priceFrom: 2868620,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/vnhlbb25urcdnlukpiky.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Free WiFi", "Air conditioning", "24/7 Front desk", "Bar"],
  },
  {
    id: "mock-hotel-5",
    name: "The Grand Metropolitan",
    city: "Phu Quoc",
    address: "50 Ocean Drive",
    rating: 4.7,
    priceFrom: 1083223,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660122/hotel_assets/dejgd2njvoknysrbhy4x.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Ocean view", "Private beach", "Kids club", "Yoga"],
  },
  {
    id: "mock-hotel-6",
    name: "The St. James Manor",
    city: "Hoi An",
    address: "60 Ocean Drive",
    rating: 4.1,
    priceFrom: 1779719,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660125/hotel_assets/ejdsqqacyt31bp2h0t0x.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "mock-hotel-7",
    name: "The Indigo House",
    city: "Da Lat",
    address: "70 Ocean Drive",
    rating: 4.8,
    priceFrom: 2324803,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/jgpukvuffxai2amh1qbt.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "mock-hotel-8",
    name: "Vela",
    city: "Ha Noi",
    address: "80 Ocean Drive",
    rating: 4.2,
    priceFrom: 1686802,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/azrfyphyu5u1vhtsot2a.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
  {
    id: "mock-hotel-9",
    name: "Airside Haven",
    city: "Da Nang",
    address: "90 Ocean Drive",
    rating: 4.2,
    priceFrom: 1400900,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/hsdob6uibiwskqfedtyr.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Free WiFi", "Air conditioning", "24/7 Front desk", "Bar"],
  },
  {
    id: "mock-hotel-10",
    name: "Timbercrest Lodge",
    city: "Ho Chi Minh City",
    address: "100 Ocean Drive",
    rating: 4,
    priceFrom: 2619817,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/ririndvquigyrk2nnlql.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Ocean view", "Private beach", "Kids club", "Yoga"],
  },
  {
    id: "mock-hotel-11",
    name: "Aura Wellness Resort",
    city: "Nha Trang",
    address: "110 Ocean Drive",
    rating: 4.8,
    priceFrom: 1965672,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/hbk4atbtappfcovzshnj.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "mock-hotel-12",
    name: "Mossgrove Nature Retreat",
    city: "Phu Quoc",
    address: "120 Ocean Drive",
    rating: 4.3,
    priceFrom: 1478624,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660121/hotel_assets/jqtr4g6fvriku4kc8ilf.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "mock-hotel-13",
    name: "The Sovereign",
    city: "Hoi An",
    address: "130 Ocean Drive",
    rating: 4.6,
    priceFrom: 2629673,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/ffyir7rr6rkqse1xtqar.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
  {
    id: "mock-hotel-14",
    name: "Oceanview Paradise",
    city: "Da Lat",
    address: "140 Ocean Drive",
    rating: 4.2,
    priceFrom: 1186200,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660120/hotel_assets/kviblu4c47q8unuixtj7.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Free WiFi", "Air conditioning", "24/7 Front desk", "Bar"],
  },
  {
    id: "mock-hotel-15",
    name: "Mountain Peak Lodge",
    city: "Ha Noi",
    address: "150 Ocean Drive",
    rating: 4.4,
    priceFrom: 2747477,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660124/hotel_assets/vzsjo3ryqg7bvuby3br8.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Ocean view", "Private beach", "Kids club", "Yoga"],
  },
  {
    id: "mock-hotel-16",
    name: "City Lights Inn",
    city: "Da Nang",
    address: "160 Ocean Drive",
    rating: 4.4,
    priceFrom: 1492195,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/yesxqezub9wfpwb7fhz4.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "mock-hotel-17",
    name: "Sunset Boulevard Hotel",
    city: "Ho Chi Minh City",
    address: "170 Ocean Drive",
    rating: 4.9,
    priceFrom: 2832207,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/tvlrqep5semsyzihwmph.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "mock-hotel-18",
    name: "Sapphire Seaside Resort",
    city: "Nha Trang",
    address: "180 Ocean Drive",
    rating: 4.1,
    priceFrom: 2403774,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660122/hotel_assets/giu7bztgp5ucffwfqam2.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
  {
    id: "mock-hotel-19",
    name: "Emerald Forest Retreat",
    city: "Phu Quoc",
    address: "190 Ocean Drive",
    rating: 4,
    priceFrom: 1735999,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/rvpgaebdwgf75iwa7hrv.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Free WiFi", "Air conditioning", "24/7 Front desk", "Bar"],
  },
  {
    id: "mock-hotel-20",
    name: "Golden Gate Hotel",
    city: "Hoi An",
    address: "200 Ocean Drive",
    rating: 4.8,
    priceFrom: 1130653,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/o3a5zfjfpu21d72mnbcr.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Ocean view", "Private beach", "Kids club", "Yoga"],
  },
  {
    id: "mock-hotel-21",
    name: "Royal Orchid Suites",
    city: "Da Lat",
    address: "210 Ocean Drive",
    rating: 4.6,
    priceFrom: 2209663,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660120/hotel_assets/odum4obtxrfenr4cmevz.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "mock-hotel-22",
    name: "Pearl Harbor Resort",
    city: "Ha Noi",
    address: "220 Ocean Drive",
    rating: 4.8,
    priceFrom: 2068143,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/t1oekaestjitmvbyffbo.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "mock-hotel-23",
    name: "Diamond Plaza Hotel",
    city: "Da Nang",
    address: "230 Ocean Drive",
    rating: 4.1,
    priceFrom: 2897791,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/fn98pag5pghavujo0l8v.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
  {
    id: "mock-hotel-24",
    name: "Crystal Bay Resort",
    city: "Ho Chi Minh City",
    address: "240 Ocean Drive",
    rating: 4.7,
    priceFrom: 1399419,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660121/hotel_assets/ypitxjiu86e57usvnecj.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Free WiFi", "Air conditioning", "24/7 Front desk", "Bar"],
  },
  {
    id: "mock-hotel-25",
    name: "Platinum Palace",
    city: "Nha Trang",
    address: "250 Ocean Drive",
    rating: 4.3,
    priceFrom: 2214861,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/lsgpar7db4ri7gtiq9qh.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Ocean view", "Private beach", "Kids club", "Yoga"],
  },
  {
    id: "mock-hotel-26",
    name: "Ruby Ridge Lodge",
    city: "Phu Quoc",
    address: "260 Ocean Drive",
    rating: 4.2,
    priceFrom: 1269777,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/j1dkywvqzbg2pprhbn2x.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Breakfast", "Airport shuttle", "Spa", "Workspace"],
  },
  {
    id: "mock-hotel-27",
    name: "Jade Garden Inn",
    city: "Hoi An",
    address: "270 Ocean Drive",
    rating: 4.4,
    priceFrom: 1767542,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/xlj37ycdddvkrzbfmhfc.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Beach view", "Pool", "Restaurant", "Family rooms"],
  },
  {
    id: "mock-hotel-28",
    name: "Topaz Tower Hotel",
    city: "Da Lat",
    address: "280 Ocean Drive",
    rating: 4.6,
    priceFrom: 2311385,
    imageUrl:
      "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/qdev7uxljmocjehljfig.jpg",
    description:
      "A premium and comfortable stay with modern amenities and beautiful views.",
    amenities: ["Meeting room", "Gym", "Late checkout", "Parking"],
  },
];

export const roomTypes: RoomType[] = [
  {
    id: "rt-hanoi-lotus",
    hotelId: "hanoi-lotus",
    name: "Hanoi Lotus Grand Room",
    capacity: 3,
    price: 1250000,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660127/hotel_assets/cckhs1da5q8xc2g5crze.jpg",
      },
    ],
  },
  {
    id: "rt-danang-bay",
    hotelId: "danang-bay",
    name: "Da Nang Bay Retreat Room",
    capacity: 2,
    price: 1680000,
    available: 11,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660130/hotel_assets/ew9wwe7ag3ujuevelb9p.jpg",
      },
    ],
  },
  {
    id: "rt-saigon-central",
    hotelId: "saigon-central",
    name: "Saigon Central Suites Room",
    capacity: 4,
    price: 1450000,
    available: 2,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660132/hotel_assets/cl9w83a0bpnuiisqldgx.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-4",
    hotelId: "mock-hotel-4",
    name: "Azure Sands Resort Room",
    capacity: 3,
    price: 2868620,
    available: 7,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/vnhlbb25urcdnlukpiky.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-5",
    hotelId: "mock-hotel-5",
    name: "The Grand Metropolitan Room",
    capacity: 2,
    price: 1083223,
    available: 11,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660122/hotel_assets/dejgd2njvoknysrbhy4x.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-6",
    hotelId: "mock-hotel-6",
    name: "The St. James Manor Room",
    capacity: 2,
    price: 1779719,
    available: 7,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660125/hotel_assets/ejdsqqacyt31bp2h0t0x.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-7",
    hotelId: "mock-hotel-7",
    name: "The Indigo House Room",
    capacity: 3,
    price: 2324803,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/jgpukvuffxai2amh1qbt.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-8",
    hotelId: "mock-hotel-8",
    name: "Vela Room",
    capacity: 4,
    price: 1686802,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/azrfyphyu5u1vhtsot2a.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-9",
    hotelId: "mock-hotel-9",
    name: "Airside Haven Room",
    capacity: 2,
    price: 1400900,
    available: 9,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/hsdob6uibiwskqfedtyr.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-10",
    hotelId: "mock-hotel-10",
    name: "Timbercrest Lodge Room",
    capacity: 3,
    price: 2619817,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/ririndvquigyrk2nnlql.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-11",
    hotelId: "mock-hotel-11",
    name: "Aura Wellness Resort Room",
    capacity: 2,
    price: 1965672,
    available: 3,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/hbk4atbtappfcovzshnj.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-12",
    hotelId: "mock-hotel-12",
    name: "Mossgrove Nature Retreat Room",
    capacity: 3,
    price: 1478624,
    available: 10,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660121/hotel_assets/jqtr4g6fvriku4kc8ilf.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-13",
    hotelId: "mock-hotel-13",
    name: "The Sovereign Room",
    capacity: 3,
    price: 2629673,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/ffyir7rr6rkqse1xtqar.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-14",
    hotelId: "mock-hotel-14",
    name: "Oceanview Paradise Room",
    capacity: 2,
    price: 1186200,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660120/hotel_assets/kviblu4c47q8unuixtj7.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-15",
    hotelId: "mock-hotel-15",
    name: "Mountain Peak Lodge Room",
    capacity: 3,
    price: 2747477,
    available: 3,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660124/hotel_assets/vzsjo3ryqg7bvuby3br8.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-16",
    hotelId: "mock-hotel-16",
    name: "City Lights Inn Room",
    capacity: 3,
    price: 1492195,
    available: 2,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/yesxqezub9wfpwb7fhz4.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-17",
    hotelId: "mock-hotel-17",
    name: "Sunset Boulevard Hotel Room",
    capacity: 3,
    price: 2832207,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/tvlrqep5semsyzihwmph.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-18",
    hotelId: "mock-hotel-18",
    name: "Sapphire Seaside Resort Room",
    capacity: 3,
    price: 2403774,
    available: 7,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660122/hotel_assets/giu7bztgp5ucffwfqam2.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-19",
    hotelId: "mock-hotel-19",
    name: "Emerald Forest Retreat Room",
    capacity: 4,
    price: 1735999,
    available: 9,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/rvpgaebdwgf75iwa7hrv.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-20",
    hotelId: "mock-hotel-20",
    name: "Golden Gate Hotel Room",
    capacity: 4,
    price: 1130653,
    available: 8,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/o3a5zfjfpu21d72mnbcr.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-21",
    hotelId: "mock-hotel-21",
    name: "Royal Orchid Suites Room",
    capacity: 4,
    price: 2209663,
    available: 5,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660120/hotel_assets/odum4obtxrfenr4cmevz.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-22",
    hotelId: "mock-hotel-22",
    name: "Pearl Harbor Resort Room",
    capacity: 3,
    price: 2068143,
    available: 6,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/t1oekaestjitmvbyffbo.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-23",
    hotelId: "mock-hotel-23",
    name: "Diamond Plaza Hotel Room",
    capacity: 4,
    price: 2897791,
    available: 2,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/fn98pag5pghavujo0l8v.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-24",
    hotelId: "mock-hotel-24",
    name: "Crystal Bay Resort Room",
    capacity: 4,
    price: 1399419,
    available: 4,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660121/hotel_assets/ypitxjiu86e57usvnecj.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-25",
    hotelId: "mock-hotel-25",
    name: "Platinum Palace Room",
    capacity: 3,
    price: 2214861,
    available: 7,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660117/hotel_assets/lsgpar7db4ri7gtiq9qh.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-26",
    hotelId: "mock-hotel-26",
    name: "Ruby Ridge Lodge Room",
    capacity: 4,
    price: 1269777,
    available: 2,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/j1dkywvqzbg2pprhbn2x.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-27",
    hotelId: "mock-hotel-27",
    name: "Jade Garden Inn Room",
    capacity: 4,
    price: 1767542,
    available: 4,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660118/hotel_assets/xlj37ycdddvkrzbfmhfc.jpg",
      },
    ],
  },
  {
    id: "rt-mock-hotel-28",
    hotelId: "mock-hotel-28",
    name: "Topaz Tower Hotel Room",
    capacity: 2,
    price: 2311385,
    available: 2,
    images: [
      {
        url: "https://res.cloudinary.com/dw8eobcaf/image/upload/v1783660119/hotel_assets/qdev7uxljmocjehljfig.jpg",
      },
    ],
  },
];
