import { Request, type Response } from './Api'

/**
Register user

POST /user/register
*/
export class RegisterRequest extends Request<RegisterResponse> {
  constructor(
    public username: string,
    public password: string,
    public cnname?: string
  ) {
    super('POST', '/user/register');
  }
}

export type RegisterResponse = Response<string>

/**
Login

POST /user/login
*/
export class LoginRequest extends Request<LoginResponse> {
  constructor(
    public readonly username: string,
    public readonly password: string
  ) {
    super('POST', '/user/login');
  }
}

export type LoginResponse = Response<{
    token: string
}>

/**
Logout

POST /user/logout
*/
export class LogoutRequest extends Request<Response<object>> {
  constructor(
    public readonly Authorization: string,
    public readonly ttl: number
  ) {
    super('POST', '/user/logout', true);
  }
}

export type LogoutResponse = Response<object>

/**
Change Password

POST /user/changePassword
*/
export class ChangePasswordRequest extends Request<ChangePasswordResponse> {
  constructor(
    public readonly username: string,
    public readonly oldPassword: string,
    public readonly newPassword: string
  ) {
    super('POST', '/user/changePassword');
  }
}

export type ChangePasswordResponse = Response<object>
