'use client';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { User, Package, Menu, X, Star } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Link } from 'react-router-dom';
import { usePathname } from '@/hooks/navigation';
import { ROUTES } from '@/constants';
const navigationItems = [
  {
    id: 'account',
    label: 'Account',
    icon: User,
    description: 'Manage your account',
    href: ROUTES.PROFILE,
  },
  {
    id: 'mybookings',
    label: 'My Bookings',
    icon: Package,
    description: 'View and manage your bookings',
    href: ROUTES.MY_BOOKINGS,
  },
  {
    id: 'myreviews',
    label: 'My Reviews',
    icon: Star,
    description: 'View and manage your reviews',
    href: ROUTES.MY_REVIEWS,
  },
];
export function AccountSidebar() {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const pathname = usePathname();
  const SidebarContent = () => (
    <Card className="p-6">
      <div className="space-y-2">
        <h2 className="text-lg font-semibold">Account Settings</h2>
        <p className="text-sm text-muted-foreground">
          Manage your account information and bookings
        </p>
      </div>
      <nav className="mt-6 space-y-1">
        {navigationItems.map((item: any) => {
          const Icon = item.icon;
          const isActive = pathname === item.href;
          return (
            <Button
              key={item.id}
              asChild
              variant={isActive ? 'secondary' : 'ghost'}
              className={cn(
                'w-full justify-start h-auto p-3 text-left whitespace-normal',
                isActive && 'bg-primary/10 text-primary border-primary/20'
              )}
              onClick={() => setIsMobileMenuOpen(false)}
            >
              <Link to={item.href}>
                <div className="flex items-center gap-3 w-full">
                  <Icon className="h-4 w-4 flex-shrink-0" />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between">
                      <span className="font-medium">{item.label}</span>
                    </div>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {item.description}
                    </p>
                  </div>
                </div>
              </Link>
            </Button>
          );
        })}
      </nav>
    </Card>
  );
  return (
    <>
      {}
      <div className="hidden md:block sticky top-24">
        <SidebarContent />
      </div>
      {}
      <div className="md:hidden mb-6">
        <Button
          variant="outline"
          onClick={() => setIsMobileMenuOpen(true)}
          className="w-full justify-start"
        >
          <Menu className="h-4 w-4 mr-2" />
          Account Settings
        </Button>
      </div>
      {}
      {isMobileMenuOpen && (
        <div className="md:hidden fixed inset-0 z-50 bg-background/80 backdrop-blur-sm">
          <div className="absolute inset-0" onClick={() => setIsMobileMenuOpen(false)}></div>
          <div className="fixed inset-y-0 left-0 w-80 bg-background border-r shadow-lg z-10">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-lg font-semibold">Account</h2>
                <Button
                  variant="ghost"
                  size="icon"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
              <SidebarContent />
            </div>
          </div>
        </div>
      )}
    </>
  );
}
