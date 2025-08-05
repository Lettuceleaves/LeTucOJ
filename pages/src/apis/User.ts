import type { Request, Response } from './Api'

/*
Register user
POST /user/register
*/
export interface RegisterRequest extends Request<RegisterResponse> {
  username: string
  password: string
  cnname?: string
}

export type RegisterResponse = Response<string>

/*
Login
POST /user/login
*/
export interface LoginRequest extends Request<LoginResponse> {
  username: string
  password: string
}

export type LoginResponse = Response<{
    token: string
}>

/*
Logout
POST /user/logout
*/
export interface LogoutRequest extends Request<Response<object>> {
  Authorization: string
  ttl: number
}

export type LogoutResponse = Response<object>

/*
Change Password
POST /user/changePassword
*/
export interface ChangePasswordRequest extends Request<ChangePasswordResponse> {
  username: string
  oldPassword: string
  newPassword: string
}

export type ChangePasswordResponse = Response<object>
