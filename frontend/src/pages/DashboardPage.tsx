import React from 'react';
import { Card } from '@/components/common';

export const DashboardPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="mt-1 text-gray-600">Welcome to the Langa Dashboard</p>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <Card title="Total Applications" padding="md">
          <p className="text-3xl font-bold text-gray-900">0</p>
          <p className="text-sm text-gray-600 mt-1">Active applications</p>
        </Card>

        <Card title="Running" padding="md">
          <p className="text-3xl font-bold text-green-600">0</p>
          <p className="text-sm text-gray-600 mt-1">Healthy instances</p>
        </Card>

        <Card title="Errors Today" padding="md">
          <p className="text-3xl font-bold text-red-600">0</p>
          <p className="text-sm text-gray-600 mt-1">Error count</p>
        </Card>

        <Card title="Avg Response Time" padding="md">
          <p className="text-3xl font-bold text-blue-600">0ms</p>
          <p className="text-sm text-gray-600 mt-1">Last 24 hours</p>
        </Card>
      </div>

      <Card title="Recent Activity" padding="md">
        <p className="text-gray-600">No recent activity</p>
      </Card>
    </div>
  );
};
