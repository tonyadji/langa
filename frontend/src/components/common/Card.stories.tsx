import type { Meta, StoryObj } from '@storybook/react-vite';
import { Card } from './Card';

const meta = {
  title: 'Common/Card',
  component: Card,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  decorators: [
    (Story) => (
      <div style={{ width: '500px' }}>
        <Story />
      </div>
    ),
  ],
} satisfies Meta<typeof Card>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    children: <p>This is the card content.</p>,
  },
};

export const WithTitle: Story = {
  args: {
    title: 'Card Title',
    children: <p>This is the card content with a title.</p>,
  },
};

export const WithSubtitle: Story = {
  args: {
    title: 'Card Title',
    subtitle: 'This is a subtitle',
    children: <p>This is the card content.</p>,
  },
};

export const WithFooter: Story = {
  args: {
    title: 'Card Title',
    children: <p>This is the card content.</p>,
    footer: <button className="text-blue-600 hover:text-blue-800">Action</button>,
  },
};

export const SmallPadding: Story = {
  args: {
    title: 'Small Padding',
    padding: 'sm',
    children: <p>This card has small padding.</p>,
  },
};

export const LargePadding: Story = {
  args: {
    title: 'Large Padding',
    padding: 'lg',
    children: <p>This card has large padding.</p>,
  },
};

export const Hoverable: Story = {
  args: {
    title: 'Hoverable Card',
    hoverable: true,
    children: <p>Hover over this card to see the effect.</p>,
  },
};

export const ComplexContent: Story = {
  args: {
    title: 'Application Status',
    subtitle: 'prod-server-01',
    children: (
      <div className="space-y-4">
        <div className="flex justify-between">
          <span className="text-gray-600">Status:</span>
          <span className="text-green-600 font-semibold">Running</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">CPU Usage:</span>
          <span className="font-semibold">45%</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Memory:</span>
          <span className="font-semibold">2.1 GB / 4 GB</span>
        </div>
      </div>
    ),
    footer: (
      <div className="flex gap-2">
        <button className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">
          Restart
        </button>
        <button className="px-3 py-1 bg-gray-200 text-gray-700 rounded hover:bg-gray-300">
          View Logs
        </button>
      </div>
    ),
  },
};
