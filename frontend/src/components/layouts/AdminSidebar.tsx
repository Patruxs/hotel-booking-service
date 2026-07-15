'use client';
  import { BedSingle, DoorOpen, Hotel, Receipt, Newspaper, Star, Mail, Settings, Coffee } from 'lucide-react';
import {
  ChevronDown,
  Home,
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { usePathname } from '@/hooks/navigation';
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible';
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuSubItem,
} from '@/components/ui/sidebar';
import { ROUTES } from '@/constants';
import { usePermission } from '@/providers/PermissionProvider';
import { filterSidebarMenu } from '@/components/layouts/adminSidebarAccess';
import type { PermissionRequirement } from '@/providers/permissionAccess';

interface NavItem extends PermissionRequirement {
  title: string;
  href: string;
  icon: React.ElementType;
  submenu?: SubMenuItem[];
}

interface SubMenuItem extends PermissionRequirement {
  title: string;
  href: string;
}

export const navItems: NavItem[] = [
  {
    title: 'Dashboard',
    href: ROUTES.ADMIN_DASHBOARD,
    icon: Home,
      requiredActions: ["reports.hotel.view"],
  },
  {
    title: 'Hotel',
    href: ROUTES.ADMIN_HOTELS,
    icon: Hotel,
    requiredActions: ["hotels.manage"],
    submenu: [
      { title: 'Hotels', href: ROUTES.ADMIN_HOTELS, requiredActions: ["hotels.manage"] },
      { title: 'Members', href: ROUTES.ADMIN_MEMBER_HOTELS, requiredRoles: ["ADMIN", "OWNER"] },
      { title: 'Inventory', href: ROUTES.ADMIN_INVENTORY, requiredRoles: ["ADMIN", "OWNER"] },
    ],
  },
    {
      title: "Room types",
    href: ROUTES.ADMIN_ROOM_TYPES,
    icon: BedSingle,
    requiredRoles: ["ADMIN", "OWNER"],
    },
    {
      title: "Rooms",
      href: ROUTES.ADMIN_ROOMS,
      icon: DoorOpen,
      requiredActions: ["rooms.view"],
    },
  {
    title: 'Bookings',
    href: ROUTES.ADMIN_BOOKINGS,
    icon: Receipt,
    requiredActions: ["bookings.list.hotel"],
  },
  {
    title: 'News',
    href: ROUTES.ADMIN_NEWS,
    icon: Newspaper,
    requiredRoles: ["ADMIN", "OWNER"],
  },
  {
    title: 'Reviews',
    href: ROUTES.ADMIN_REVIEWS,
    icon: Star,
    requiredActions: ["reviews.manage"],
  },
  {
    title: 'Contacts',
    href: ROUTES.ADMIN_CONTACTS,
    icon: Mail,
    requiredRoles: ["ADMIN"],
  },
  {
    title: 'Amenities',
    href: ROUTES.ADMIN_AMENITIES,
    icon: Coffee,
    requiredRoles: ["ADMIN", "OWNER", "MANAGER"],
  },
  {
    title: 'Settings',
    href: ROUTES.ADMIN_SETTINGS,
    icon: Settings,
    requiredRoles: ["ADMIN"],
  },
];

export default function AdminSidebar() {
  const pathname = usePathname();
  const { canAccess } = usePermission();
  const filteredMenu = filterSidebarMenu(navItems, canAccess);
  return (
    <Sidebar collapsible="icon" className="h-screen">
      <SidebarHeader>
        <SidebarGroup>
          <SidebarGroupLabel>
            <h1 className="text-2xl text-black font-bold">Admin Panel</h1>
          </SidebarGroupLabel>
        </SidebarGroup>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupContent>
            <SidebarMenu>
                {filteredMenu.map((item) => {
                const isActive =
                  pathname === item.href ||
                  pathname?.startsWith(item.href + '/');
                if (item.submenu) {
                  return (
                    <Collapsible
                      key={item.title}
                      defaultOpen={pathname?.startsWith(item.href)}
                    >
                      <SidebarMenuItem>
                        <CollapsibleTrigger asChild>
                          <SidebarMenuButton isActive={isActive}>
                            <item.icon className="h-4 w-4" />
                            <span>{item.title}</span>
                            <ChevronDown className="ml-auto h-4 w-4 shrink-0 transition-transform duration-200 group-data-[state=open]/collapsible:rotate-180" />
                          </SidebarMenuButton>
                        </CollapsibleTrigger>
                        <CollapsibleContent>
                          <SidebarMenuSub>
                              {item.submenu.map((subItem) => (
                              <SidebarMenuSubItem key={subItem.title}>
                                <SidebarMenuSubButton
                                  asChild
                                  isActive={pathname === subItem.href}
                                >
                                  <Link to={subItem.href}>
                                    {subItem.title}
                                  </Link>
                                </SidebarMenuSubButton>
                              </SidebarMenuSubItem>
                            ))}
                          </SidebarMenuSub>
                        </CollapsibleContent>
                      </SidebarMenuItem>
                    </Collapsible>
                  );
                }
                return (
                  <SidebarMenuItem key={item.title}>
                    <SidebarMenuButton asChild isActive={isActive}>
                      <Link to={item.href}>
                        <item.icon className="h-4 w-4" />
                        <span>{item.title}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      {
}
    </Sidebar>
  );
}
