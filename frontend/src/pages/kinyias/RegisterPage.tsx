'use client';
import { useState } from 'react';
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import { MailCheckIcon, ArrowRight } from 'lucide-react';
import PageTitle from '@/components/sections/PageTitle';
import { ROUTES } from '@/constants';
import RegisterForm from '@/features/auth/components/RegisterForm';
export default function RegisterPage() {
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [email, setEmail] = useState('');
  return (
    <>
      <PageTitle
        title="Sign Up"
        breadcrumbs={[
          { label: 'Home', href: ROUTES.HOME },
          { label: 'Sign Up', href: ROUTES.REGISTER },
        ]}
      />
      {!isSubmitted ? (
        <div className="py-20 flex items-center justify-center px-4 sm:px-6 lg:px-8">
          <Card className="w-full max-w-xl">
            <CardHeader>
              <CardTitle className="text-2xl font-bold text-center">
                Create an Account
              </CardTitle>
              <CardDescription className="text-center">
                Sign in with your new account
              </CardDescription>
            </CardHeader>
            <CardContent>
                <RegisterForm
                  onSuccess={(email) => {
                    setEmail(email);
                    setIsSubmitted(true);
                  }}
                />
              </CardContent>
            <CardFooter className="flex justify-center">
              <p className="text-sm text-gray-600">
                Already have an account?{' '}
                <Link
                  to={ROUTES.LOGIN}
                  className="text-primary hover:underline"
                >
                  Log in
                </Link>
              </p>
            </CardFooter>
          </Card>
        </div>
      ) : (
        <div className="w-full h-[80vh] flex flex-col gap-2 items-center justify-center rounded-md">
          <MailCheckIcon size="48px" className="animate-bounce" />
          <h2 className="text-xl font-bold">Check your email</h2>
          <p className="mb-2 text-center text-sm text-muted-foreground">
            We’ve sent a verification link to <strong>{email}</strong>.
          </p>
          <Link to={ROUTES.LOGIN}>
            <Button className="h-[40px]">
              Continue to Login <ArrowRight />
            </Button>
          </Link>
        </div>
      )}
    </>
  );
}
