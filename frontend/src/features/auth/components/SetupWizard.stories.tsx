import type { Meta, StoryObj } from '@storybook/react';
import { SetupWizard } from './SetupWizard';

const meta = {
  title: 'Features/Auth/SetupWizard',
  component: SetupWizard,
  decorators: [
    (Story) => (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <Story />
      </div>
    ),
  ],
  tags: ['autodocs'],
} satisfies Meta<typeof SetupWizard>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    onComplete: () => alert('Setup complete!'),
    onSkip: () => alert('Setup skipped'),
  },
};

export const WithoutSkip: Story = {
  args: {
    onComplete: () => alert('Setup complete!'),
  },
};

export const CustomHandlers: Story = {
  args: {
    onComplete: () => console.log('Wizard completed'),
    onSkip: () => console.log('Wizard skipped'),
  },
};
