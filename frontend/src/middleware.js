import { NextResponse } from 'next/server';

// Routes that require authentication
const PROTECTED_ROUTES = ['/dashboard'];

// Routes that are public (skip auth check)
const PUBLIC_ROUTES = ['/', '/login', '/register', '/about', '/product', '/features', '/pricing', '/contact', '/demo'];

export function middleware(request) {
    const { pathname } = request.nextUrl;

    // Check if the route requires auth
    const isProtected = PROTECTED_ROUTES.some(route => pathname.startsWith(route));

    if (!isProtected) {
        return NextResponse.next();
    }

    // Check for token in cookies (preferred) — localStorage is not accessible in middleware (server-side).
    // The frontend login must also set a cookie so middleware can read it.
    const token = request.cookies.get('hrms_token')?.value;

    if (!token) {
        // No token — redirect to login with redirect param
        const loginUrl = new URL('/login', request.url);
        loginUrl.searchParams.set('redirect', pathname);
        return NextResponse.redirect(loginUrl);
    }

    // Token exists — allow request to proceed
    // (Full JWT validation is done by the backend on API calls)
    return NextResponse.next();
}

export const config = {
    matcher: [
        /*
         * Match all request paths EXCEPT:
         * - api routes
         * - _next/static (static files)
         * - _next/image (optimized images)
         * - favicon
         */
        '/((?!api|_next/static|_next/image|favicon.ico).*)',
    ],
};
