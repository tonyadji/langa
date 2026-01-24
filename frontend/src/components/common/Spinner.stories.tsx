import type { Meta, StoryObj } from '@storybook/react-vite';
import { Spinner, LoadingSpinner } from './Spinner';

const meta = {
  title: 'Common/Spinner',
  component: Spinner,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof Spinner>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {},
};

export const Small: Story = {
  args: {
    size: 'sm',
  },
};

export const Large: Story = {
  args: {
    size: 'lg',
  },
};

export const ExtraLarge: Story = {
  args: {
    size: 'xl',
  },
};

export const CustomColor: Story = {
  args: {
    color: 'text-red-600',
  },
};

export const WithMessage: Story = {
  render: () => <LoadingSpinner message="Loading data..." />,
};
