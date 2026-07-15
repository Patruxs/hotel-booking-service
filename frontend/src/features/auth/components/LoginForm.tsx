'use client';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Mail, Lock, EyeOff, Eye, Loader } from 'lucide-react';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '@/providers/AuthProvider';
import { loginSchema } from '@/features/auth';
import type * as z from 'zod';
import { ROUTES } from '@/constants';
import { hasManagementShellRole } from '@/providers/permissionAccess';
type LoginFormValues = z.infer<typeof loginSchema>;
export function LoginForm() {
  const { loading, login } = useAuth();
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const from = typeof location.state === "object" && location.state && "from" in location.state
    ? String(location.state.from)
    : ROUTES.HOME;
  const form = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });
  const onSubmit = async (values: LoginFormValues) => {
    try {
      const user = await login(values.email, values.password);
        const hasAdminShellRole = hasManagementShellRole(
          user?.roles?.map((role) => role.name) ?? [],
        );

      if (hasAdminShellRole) {
        navigate('/admin/', { replace: true });
      } else {
        navigate(from, { replace: true });
      }
    } catch (error) {
      console.error('Login error:', error);
    }
  };
  return (
    <>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          {/* Email */}
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email</FormLabel>
                <div className="relative">
                  <Mail className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                  <FormControl>
                    <Input
                      placeholder="Email"
                      className="pl-10"
                      disabled={loading}
                      {...field}
                    />
                  </FormControl>
                </div>
                <FormMessage />
              </FormItem>
            )}
          />
          {/* Mật khẩu */}
          <FormField
            control={form.control}
            name="password"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="flex items-center justify-between">
                  <span>Password</span>
                  <Link
                    to={ROUTES.FORGOT_PASSWORD}
                    className="text-sm text-primary hover:underline"
                  >
                    Forgot password?
                  </Link>
                </FormLabel>
                <div className="relative">
                  <Lock className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                  <FormControl>
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="Enter your password"
                      className="pl-10"
                      disabled={loading}
                      {...field}
                    />
                  </FormControl>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShowPassword(!showPassword)}
                    disabled={loading}
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4 text-gray-400" />
                    ) : (
                      <Eye className="h-4 w-4 text-gray-400" />
                    )}
                    <span className="sr-only">
                      {showPassword ? 'Hide password' : 'Show password'}
                    </span>
                  </Button>
                </div>
                <FormMessage />
              </FormItem>
            )}
          />
          {}
          <Button
            type="submit"
            className="w-full cursor-pointer"
            disabled={loading}
          >
            {loading ? (
              <>
                <Loader className="animate-spin mr-2 h-4 w-4" />
                Logging...
              </>
            ) : (
              'Login'
            )}
          </Button>
        </form>
      </Form>
      </>
    );
  }
