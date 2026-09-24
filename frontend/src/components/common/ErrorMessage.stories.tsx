import type { Meta, StoryObj } from '@storybook/react-vite';
import { ErrorMessage } from './ErrorMessage';

const meta = {
  title: 'Common/ErrorMessage',
  component: ErrorMessage,
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
} satisfies Meta<typeof ErrorMessage>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    message: 'Something went wrong. Please try again.',
  },
};

export const WithCustomTitle: Story = {
  args: {
    title: 'Failed to load applications',
    message: 'Unable to fetch applications from the server. Please check your connection.',
  },
};

export const WithRetry: Story = {
  args: {
    title: 'Network Error',
    message: 'Could not connect to the server. Please check your internet connection.',
    onRetry: () => alert('Retrying...'),
  },
};

export const LongMessage: Story = {
  args: {
    title: 'Validation Failed',
    message:
      'The operation could not be completed due to multiple validation errors. Please ensure all required fields are filled out correctly and that your input meets the specified criteria before trying again.',
  },
};
