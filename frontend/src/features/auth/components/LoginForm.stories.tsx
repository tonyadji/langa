import type { Meta, StoryObj } from '@storybook/react';
import { BrowserRouter } from 'react-router-dom';
import { LoginForm } from './LoginForm';

const meta = {
  title: 'Features/Auth/LoginForm',
  component: LoginForm,
  decorators: [
    (Story) => (
      <BrowserRouter>
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
          <Story />
        </div>
      </BrowserRouter>
    ),
  ],
  tags: ['autodocs'],
} satisfies Meta<typeof LoginForm>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};

export const WithError: Story = {
  args: {
    onSubmit: async () => {
      throw new Error('Invalid username or password');
    },
  },
};

export const Loading: Story = {
  args: {
    onSubmit: async () => {
      return new Promise((resolve) => setTimeout(resolve, 3000));
    },
  },
};

export const Successful: Story = {
  args: {
    onSubmit: async (data) => {
      console.log('Login data:', data);
      alert(`Login successful for ${data.username}`);
    },
  },
};
