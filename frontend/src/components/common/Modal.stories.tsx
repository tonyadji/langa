import { useState } from 'react';
import type { Meta, StoryObj } from '@storybook/react-vite';
import { Modal } from './Modal';
import { Button } from './Button';

const meta = {
  title: 'Common/Modal',
  component: Modal,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof Modal>;

export default meta;
type Story = StoryObj<typeof meta>;

const ModalWithTrigger = (args: React.ComponentProps<typeof Modal>) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <>
      <Button onClick={() => setIsOpen(true)}>Open Modal</Button>
      <Modal {...args} isOpen={isOpen} onClose={() => setIsOpen(false)} />
    </>
  );
};

export const Default: Story = {
  args: { isOpen: false, onClose: () => {}, children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger isOpen={false} onClose={() => {}}>
      <p>This is the modal content.</p>
    </ModalWithTrigger>
  ),
};

export const WithTitle: Story = {
  args: { isOpen: false, onClose: () => {}, title: 'Modal Title', children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger isOpen={false} onClose={() => {}} title="Modal Title">
      <p>This is the modal content with a title.</p>
    </ModalWithTrigger>
  ),
};

export const WithFooter: Story = {
  args: { isOpen: false, onClose: () => {}, title: 'Confirm Action', children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger
      isOpen={false}
      onClose={() => {}}
      title="Confirm Action"
      footer={
        <>
          <Button variant="secondary">Cancel</Button>
          <Button variant="primary">Confirm</Button>
        </>
      }
    >
      <p>Are you sure you want to proceed?</p>
    </ModalWithTrigger>
  ),
};

export const SmallSize: Story = {
  args: { isOpen: false, onClose: () => {}, title: 'Small Modal', size: 'sm', children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger isOpen={false} onClose={() => {}} title="Small Modal" size="sm">
      <p>This is a small modal.</p>
    </ModalWithTrigger>
  ),
};

export const LargeSize: Story = {
  args: { isOpen: false, onClose: () => {}, title: 'Large Modal', size: 'lg', children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger isOpen={false} onClose={() => {}} title="Large Modal" size="lg">
      <div className="space-y-4">
        <p>This is a large modal with more content.</p>
        <p>It can contain multiple paragraphs and elements.</p>
        <p>The content will scroll if it exceeds the maximum height.</p>
      </div>
    </ModalWithTrigger>
  ),
};

export const ComplexContent: Story = {
  args: { isOpen: false, onClose: () => {}, title: 'Complex Form', children: <p>Sample</p> },
  render: () => (
    <ModalWithTrigger
      isOpen={false}
      onClose={() => {}}
      title="Delete Application"
      size="md"
      footer={
        <>
          <Button variant="secondary">Cancel</Button>
          <Button variant="danger">Delete</Button>
        </>
      }
    >
      <div className="space-y-4">
        <p className="text-gray-700">
          Are you sure you want to delete this application? This action cannot be undone.
        </p>
        <div className="bg-yellow-50 border border-yellow-200 rounded-md p-4">
          <p className="text-sm text-yellow-800">
            <strong>Warning:</strong> All associated data, logs, and metrics will be permanently deleted.
          </p>
        </div>
      </div>
    </ModalWithTrigger>
  ),
};
