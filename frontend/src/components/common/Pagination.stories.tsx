import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { Pagination } from './Pagination';

const meta = {
  title: 'Common/Pagination',
  component: Pagination,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
} satisfies Meta<typeof Pagination>;

export default meta;
type Story = StoryObj<typeof meta>;

const PaginationWithState = (args: React.ComponentProps<typeof Pagination>) => {
  const [currentPage, setCurrentPage] = useState(args.currentPage);

  return (
    <Pagination
      {...args}
      currentPage={currentPage}
      onPageChange={(page) => {
        setCurrentPage(page);
        args.onPageChange(page);
      }}
    />
  );
};

export const Default: Story = {
  render: (args) => <PaginationWithState {...args} />,
  args: {
    currentPage: 1,
    totalPages: 10,
    onPageChange: (page) => console.log('Page changed to:', page),
  },
};

export const FewPages: Story = {
  render: (args) => <PaginationWithState {...args} />,
  args: {
    currentPage: 2,
    totalPages: 5,
    onPageChange: (page) => console.log('Page changed to:', page),
  },
};

export const ManyPages: Story = {
  render: (args) => <PaginationWithState {...args} />,
  args: {
    currentPage: 15,
    totalPages: 50,
    onPageChange: (page) => console.log('Page changed to:', page),
  },
};

export const FirstPage: Story = {
  render: (args) => <PaginationWithState {...args} />,
  args: {
    currentPage: 1,
    totalPages: 20,
    onPageChange: (page) => console.log('Page changed to:', page),
  },
};

export const LastPage: Story = {
  render: (args) => <PaginationWithState {...args} />,
  args: {
    currentPage: 20,
    totalPages: 20,
    onPageChange: (page) => console.log('Page changed to:', page),
  },
};
