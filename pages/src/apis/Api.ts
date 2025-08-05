import axios, { type AxiosRequestConfig } from 'axios'

export interface Request<T> {
  readonly __responseType?: T // 仅用于帮助 TypeScript 推断
}

export interface Response<T> {
  status: number;
  data: T | null;
  error: string | null;
}

type ExtractResponse<T> = T extends Request<infer R> ? R : never


const baseUrl = "http://letucoj.cn:7777";

export async function post<T extends Request<unknown>>(
  path: string,
  data: T,
  config?: AxiosRequestConfig<unknown>,
): Promise<ExtractResponse<T>> {
  return await axios.post<T, ExtractResponse<T>>(`${baseUrl}/${path}`, data, config)
}
