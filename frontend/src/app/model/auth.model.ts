export type UserRole = 'BRANCH_MANAGER' | 'SUPER_MANAGER';


export interface AuthModel {
  token: string;
  username: string;
  role: 'SUPER_MANAGER' | 'BRANCH_MANAGER';
  name?: string;
}
