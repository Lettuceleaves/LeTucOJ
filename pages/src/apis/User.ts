import type { Request, Response } from './Api'

// Register
export interface RegisterRequest extends Request<RegisterResponse> {
  username: string
  password: string
  cnname?: string
}

export interface RegisterResponse extends Response<string> {}

// Login
export interface LoginRequest extends Request<LoginResponse> {
  username: string
  password: string
}

export interface LoginResponse
  extends Response<{
    token: string
  }> {}

// Logout
export interface LogoutRequest extends Request<LogoutResponse> {
  Authorization: string
  ttl: number
}

export interface LogoutResponse extends Response<{}> {}

// Change Password
export interface ChangePasswordRequest extends Request<ChangePasswordResponse> {
  username: string
  oldPassword: string
  newPassword: string
}

export interface ChangePasswordResponse extends Response<{}> {}
