import type { Meta, StoryObj } from '@storybook/react-vite';
import { FileText, Users, Inbox } from 'lucide-react';
import { EmptyState } from './EmptyState';
import { Button } from './Button';

const meta = {
  title: 'Common/EmptyState',
  component: EmptyState,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <div style={{ width: '600px' }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof EmptyState>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    title: 'No data available',
  },
};

export const WithDescription: Story = {
  args: {
    title: 'No applications found',
    description: 'Get started by creating your first application to monitor.',
  },
};

export const WithIcon: Story = {
  args: {
    title: 'No logs yet',
    description: 'Start your application to see logs appear here.',
    icon: <FileText size={48} className="text-gray-400" />,
  },
};

export const WithAction: Story = {
  args: {
    title: 'No team members',
    description: 'Invite team members to collaborate on this project.',
    icon: <Users size={48} className="text-gray-400" />,
    action: <Button variant="primary">Invite Team Members</Button>,
  },
};

export const CompleteExample: Story = {
  args: {
    title: 'No metrics available',
    description: 'Deploy your application and start monitoring its performance metrics in real-time.',
    icon: <Inbox size={48} className="text-gray-400" />,
    action: (
      <div className="flex gap-2">
        <Button variant="secondary">Learn More</Button>
        <Button variant="primary">Deploy Application</Button>
      </div>
    ),
  },
};
