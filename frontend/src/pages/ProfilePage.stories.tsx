import type { Meta, StoryObj } from '@storybook/react';
import { BrowserRouter } from 'react-router-dom';
import { ProfilePage } from './ProfilePage';
import { AuthContext } from '@/features/auth/context/AuthContext';

const mockAuthContextValue = {
  user: {
    email: 'john.doe@example.com',
    accountKey: '550e8400-e29b-41d4-a716-446655440000',
    role: 'USER',
    firstConnection: false,
    registrationDate: '2024-12-01T10:30:00Z',
  },
  isAuthenticated: true,
  isLoading: false,
  accessToken: 'mock-access-token',
  refreshToken: 'mock-refresh-token',
  login: async () => {},
  register: async () => {},
  logout: async () => {
    console.log('Logout clicked');
  },
  refreshTokens: async () => {},
  updateUser: async () => {},
  fetchUserProfile: async () => {},
};

const meta = {
  title: 'Pages/ProfilePage',
  component: ProfilePage,
  decorators: [
    (Story) => (
      <BrowserRouter>
        <AuthContext.Provider value={mockAuthContextValue}>
          <Story />
        </AuthContext.Provider>
      </BrowserRouter>
    ),
  ],
  tags: ['autodocs'],
  parameters: {
    layout: 'fullscreen',
  },
} satisfies Meta<typeof ProfilePage>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

export const NewUser: Story = {
  decorators: [
    (Story) => (
      <BrowserRouter>
        <AuthContext.Provider
          value={{
            ...mockAuthContextValue,
            user: {
              email: 'newuser@example.com',
              accountKey: '123e4567-e89b-12d3-a456-426614174000',
              role: 'USER',
              firstConnection: true,
              registrationDate: new Date().toISOString(),
            },
          }}
        >
          <Story />
        </AuthContext.Provider>
      </BrowserRouter>
    ),
  ],
};

export const LongEmail: Story = {
  decorators: [
    (Story) => (
      <BrowserRouter>
        <AuthContext.Provider
          value={{
            ...mockAuthContextValue,
            user: {
              email: 'very.long.email.address.for.testing@verylongdomainname.example.com',
              accountKey: '987e6543-e21b-98d7-a654-321456987000',
              role: 'USER',
              firstConnection: false,
              registrationDate: '2023-06-15T14:20:00Z',
            },
          }}
        >
          <Story />
        </AuthContext.Provider>
      </BrowserRouter>
    ),
  ],
};
