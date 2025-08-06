import { getJwtToken } from '@/persistence/LocalPersistence';
import axios, { type AxiosRequestConfig } from 'axios'

export class Request<T> {
  readonly __responseType?: T // 仅用于帮助 TypeScript 推断

  constructor(
    public readonly method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    public readonly path: string,
    public readonly authorized: boolean = false
  ) { }

  private getRequestConfig(): AxiosRequestConfig | undefined {
    if (!this.authorized) return;

    return {
      headers: {
        Authorization: `Bearer ${getJwtToken()}`
      }
    }
  }

  async request(): Promise<T> {
    const config = this.getRequestConfig();
    if (this.method === 'GET') {
      return await get(this.path, config) as T;
    } else if (this.method === 'POST') {
      return await post(this.path, this, config) as T;
    }

    // TODO: 支持其他 HTTP 方法
    throw new Error(`Unsupported method: ${this.method}`);
  }
}

export interface Response<T> {
  status: number
  data: T | null
  error: string | null
}

type ExtractResponse<T> = T extends Request<infer R> ? R : never

const baseUrl = 'http://letucoj.cn:7777'

function getUrl(path: string): string {
  return path.startsWith('/') ? `${baseUrl}${path}` : `${baseUrl}/${path}`;
}

export async function get<T extends Request<unknown>>(
  path: string,
  config?: AxiosRequestConfig<unknown>,
): Promise<ExtractResponse<T>> {
  return await axios.get(getUrl(path), config);
}

export async function post<T extends Request<unknown>>(
  path: string,
  data: T,
  config?: AxiosRequestConfig<unknown>,
): Promise<ExtractResponse<T>> {
  return await axios.post<T, ExtractResponse<T>>(getUrl(path), data, config)
}
