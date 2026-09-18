import type { Meta, StoryObj } from '@storybook/react-vite';
import { Table } from './Table';
import { Badge } from './Badge';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  status: 'active' | 'inactive';
}

const sampleData: User[] = [
  { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Admin', status: 'active' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'Developer', status: 'active' },
  { id: 3, name: 'Bob Johnson', email: 'bob@example.com', role: 'Viewer', status: 'inactive' },
];

const meta: Meta<typeof Table<User>> = {
  title: 'Common/Table',
  component: Table,
  parameters: {
    layout: 'padded',
  },
  tags: ['autodocs'],
};

export default meta;
type Story = StoryObj<typeof Table<User>>;

export const Default: Story = {
  args: {
    data: sampleData,
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'email', header: 'Email' },
      { key: 'role', header: 'Role' },
      { key: 'status', header: 'Status' },
    ],
  },
};

export const WithCustomRenderers: Story = {
  args: {
    data: sampleData,
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'email', header: 'Email' },
      { key: 'role', header: 'Role' },
      {
        key: 'status',
        header: 'Status',
        render: (user: User) => (
          <Badge variant={user.status === 'active' ? 'success' : 'default'}>
            {user.status}
          </Badge>
        ),
      },
    ],
  },
};

export const Empty: Story = {
  args: {
    data: [],
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'email', header: 'Email' },
    ],
    emptyMessage: 'No users found',
  },
};

export const Clickable: Story = {
  args: {
    data: sampleData,
    columns: [
      { key: 'name', header: 'Name' },
      { key: 'email', header: 'Email' },
      { key: 'role', header: 'Role' },
    ],
    onRowClick: (user: User) => alert(`Clicked on ${user.name}`),
  },
};
