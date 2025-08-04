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

export function getJwt(): JwtPayload | undefined {
  const token = localStorage.getItem(JwtKey);
  if (token == null) return;

  try {
    let payload = jwtDecode<JwtPayload>(token);

    const expires = new Date(payload.exp * 1000);
    if (expires < new Date()) {
      localStorage.removeItem(JwtKey);
      return;
    }

    return payload;
  } catch (error: any) {
    console.log("无法解析 JWT", error);
  }
}
