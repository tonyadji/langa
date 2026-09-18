import type { Meta, StoryObj } from '@storybook/react';
import { BrowserRouter } from 'react-router-dom';
import { RegisterForm } from './RegisterForm';

const meta = {
  title: 'Features/Auth/RegisterForm',
  component: RegisterForm,
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
} satisfies Meta<typeof RegisterForm>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};

export const WithDuplicateEmailError: Story = {
  args: {
    onSubmit: async () => {
      throw new Error('Email already registered');
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
      console.log('Registration data:', data);
      alert(`Registration successful for ${data.username}`);
    },
  },
};
