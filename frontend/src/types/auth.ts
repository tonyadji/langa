export interface User {
  id: string;
  email: string;
  name: string;
  role: 'admin' | 'developer' | 'viewer';
  createdAt: string;
  updatedAt: string;
}
