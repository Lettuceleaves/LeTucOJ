import { getJwt } from '@/persistence/LocalPersistence'
import axios, { type AxiosResponse } from 'axios'

const baseUrl = 'http://letucoj.cn:7777'

export class Request<T> {
  readonly __responseType?: T // 仅用于帮助 TypeScript 推断

  constructor(
    public readonly method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    public readonly path: string,
    public readonly authorized: boolean = false,
  ) {}

  private getAuthorization(): string | undefined {
    if (!this.authorized) return

    return `Bearer ${getJwt()}`
  }

  private getUrl(): string {
    const path = this.path.startsWith('/') ? this.path.slice(1) : this.path
    return `${baseUrl}/${path}`
  }

  protected getData() {
    return {
      ...this,
      method: undefined,
      path: undefined,
      authorized: undefined
    }
  }

  protected getBody() {
    if (this.method === 'GET' || this.method === 'DELETE') return
    return this.getData()
  }

  protected getParams() {
    if (this.method === 'PUT' || this.method === 'POST') return
    return this.getData()
  }

  async request(): Promise<T> {
    const resp = await axios.request<this, AxiosResponse<T>, this>({
      method: this.method,
      url: this.getUrl(),
      data: this.getBody(),
      params: this.getParams(),
      headers: {
        Authorization: this.getAuthorization()
      }
    })

    return resp.data;
  }
}

export interface Response<T> {
  status: number
  data: T | null
  error: string | null
}
