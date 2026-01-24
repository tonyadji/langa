import type { Meta, StoryObj } from '@storybook/react';
import { Alert } from './Alert';

const meta = {
  title: 'Components/Common/Alert',
  component: Alert,
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: 'select',
      options: ['success', 'error', 'warning', 'info'],
    },
  },
} satisfies Meta<typeof Alert>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Success: Story = {
  args: {
    variant: 'success',
    children: 'Your changes have been saved successfully!',
  },
};

export const Error: Story = {
  args: {
    variant: 'error',
    children: 'An error occurred while processing your request. Please try again.',
  },
};

export const Warning: Story = {
  args: {
    variant: 'warning',
    children: 'This action cannot be undone. Please proceed with caution.',
  },
};

export const Info: Story = {
  args: {
    variant: 'info',
    children: 'Your session will expire in 5 minutes. Please save your work.',
  },
};

export const LongMessage: Story = {
  args: {
    variant: 'error',
    children:
      'There was a problem connecting to the server. This could be due to network issues, server maintenance, or an invalid configuration. Please check your internet connection and try again in a few minutes.',
  },
};
