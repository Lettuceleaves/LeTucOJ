import type { UserInfo } from '@/models/User'
import { jwtDecode } from 'jwt-decode'

const JwtKey = 'jwt'

export interface JwtPayload {
  exp: number
  iat: number
  role: 'ROOT' | 'MANAGER' | 'USER'
  sub: string
  cnname: string
}

export function persistJwt(jwt: string) {
  localStorage.setItem(JwtKey, jwt)
}

export function clearJwt() {
  localStorage.removeItem(JwtKey)
}

export function getJwt(): string | null {
  return localStorage.getItem(JwtKey)
}

export function getDecodedJwt(): UserInfo | null {
  const token = localStorage.getItem(JwtKey)
  if (token == null) return null

  try {
    let payload = jwtDecode<JwtPayload>(token)

    const expires = new Date(payload.exp * 1000)
    if (expires < new Date()) {
      clearJwt()
      throw new Error('Expired token')
    }

    return {
      name: payload.sub,
      cnname: payload.cnname,
      role: payload.role
    }
  } catch (error: unknown) {
    console.log('无法解析 JWT', error)
    return null
  }
}
