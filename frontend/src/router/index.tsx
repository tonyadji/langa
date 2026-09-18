import { Routes, Route, Navigate } from 'react-router-dom';
import { lazy, Suspense } from 'react';
import { Layout } from '@/components/layout';
import { ProtectedRoute } from '@/router/ProtectedRoute';
import { PageSkeleton } from '@/components/Skeleton';

// Lazy load pages for code splitting
const DashboardPage = lazy(() => import('@/pages/DashboardPage').then(m => ({ default: m.DashboardPage })));
const ApplicationsPage = lazy(() => import('@/pages/ApplicationsPage').then(m => ({ default: m.ApplicationsPage })));
const ApplicationDetailsPage = lazy(() => import('@/pages/ApplicationDetailsPage').then(m => ({ default: m.ApplicationDetailsPage })));
const LogsPage = lazy(() => import('@/pages/LogsPage').then(m => ({ default: m.LogsPage })));
const MetricsPage = lazy(() => import('@/pages/MetricsPage').then(m => ({ default: m.MetricsPage })));
const TeamsPage = lazy(() => import('@/pages/TeamsPage').then(m => ({ default: m.TeamsPage })));
const TeamDetailsPage = lazy(() => import('@/pages/TeamDetailsPage').then(m => ({ default: m.TeamDetailsPage })));
const AcceptInvitationPage = lazy(() => import('@/pages/AcceptInvitationPage').then(m => ({ default: m.AcceptInvitationPage })));
const LoginPage = lazy(() => import('@/pages/LoginPage').then(m => ({ default: m.LoginPage })));
const RegisterPage = lazy(() => import('@/pages/RegisterPage').then(m => ({ default: m.RegisterPage })));
const ProfilePage = lazy(() => import('@/pages/ProfilePage').then(m => ({ default: m.ProfilePage })));

export function AppRouter() {
  return (
    <Suspense fallback={<PageSkeleton />}>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/accept-invitation/:teamId/:token" element={<AcceptInvitationPage />} />
        
        {/* Protected routes with layout */}
        <Route
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/applications" element={<ApplicationsPage />} />
          <Route path="/applications/:appId" element={<ApplicationDetailsPage />} />
          <Route path="/logs" element={<LogsPage />} />
          <Route path="/metrics" element={<MetricsPage />} />
          <Route path="/teams" element={<TeamsPage />} />
          <Route path="/teams/:teamId" element={<TeamDetailsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>
      </Routes>
    </Suspense>
  );
}

