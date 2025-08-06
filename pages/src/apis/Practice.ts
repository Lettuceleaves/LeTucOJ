import type { PraciceInfo, SimplePracticeInfo } from '@/models/Practice'
import { Request, type Response } from './Api'

/**
Get practice list

POST /practice/list
*/
export class GetPracticeListRequest extends Request<GetPracticeListResponse> {
  constructor(
    public readonly start: number,
    public readonly limit: number,
    public readonly order: string,
    public readonly like: string
  ) {
    super('GET', '/practice/list', true);
  }
}

export type GetPracticeListResponse = Response<SimplePracticeInfo[]>

/**
Get amount of practices

GET /practice/count
*/
export class GetPracticesCountRequest extends Request<GetPracticesCountResponse> {
  constructor() {
    super('GET', '/practice/count', true);
  }
}

export type GetPracticesCountResponse = Response<number>;

/**
Search practice

POST /practice/searchList
*/
export class SearchPracticeRequest extends Request<SearchPracticeRequest> {
  constructor(
    public readonly start: number,
    public readonly limit: number,
    public readonly order: string,
    public readonly like: string
  ) {
    super('POST', '/practice/searchList', true);
  }
}

export type SearchPracticeResponse = Response<SimplePracticeInfo[]>

/**
Get pratice detail

POST /practice/full/get
*/
export class GetPracticeDetailRequest extends Request<GetPracticeDetailResponse> {
  constructor(
    public readonly name: string
  ) {
    super('POST', '/practice/full/get', true);
  }
}

export type GetPracticeDetailResponse = Response<PraciceInfo>
