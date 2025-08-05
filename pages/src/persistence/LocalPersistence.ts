import { jwtDecode } from "jwt-decode";

const JwtKey = 'jwt';

export interface JwtPayload {
  exp: number;
  iat: number;
  role: string;
  sub: string;
  username: string;
}

export function persistJwt(jwt: string) {
  localStorage.setItem(JwtKey, jwt);
}

export function getJwtToken(): string | null {
  return localStorage.getItem(JwtKey);
}

export function getDecodedJwt(): JwtPayload | null {
  const token = localStorage.getItem(JwtKey);
  if (token == null) return null;

  try {
    let payload = jwtDecode<JwtPayload>(token);

    const expires = new Date(payload.exp * 1000);
    if (expires < new Date()) {
      localStorage.removeItem(JwtKey);
      throw new Error('Expired token');
    }

    return payload;
  } catch (error: unknown) {
    console.log("无法解析 JWT", error);
    return null;
  }
}
