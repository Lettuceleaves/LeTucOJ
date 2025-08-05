import type { Request, Response } from './Api'

// Register
export interface RegisterRequest extends Request<RegisterResponse> {
  username: string
  password: string
  cnname?: string
}

export type RegisterResponse = Response<string>

// Login
export interface LoginRequest extends Request<LoginResponse> {
  username: string
  password: string
}

export type LoginResponse = Response<{
    token: string
}>

// Logout
export interface LogoutRequest extends Request<Response<object>> {
  Authorization: string
  ttl: number
}

export type LogoutResponse = Response<object>

// Change Password
export interface ChangePasswordRequest extends Request<ChangePasswordResponse> {
  username: string
  oldPassword: string
  newPassword: string
}

export type ChangePasswordResponse = Response<object>
