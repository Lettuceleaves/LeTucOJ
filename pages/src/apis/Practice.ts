import type { PraciceInfo, SimplePracticeInfo } from '@/models/Practice'
import { Request, type Response } from './Api'
import type { AxiosRequestConfig } from 'axios';

/**
Get practice list

POST /practice/list
*/
export class GetPracticeListRequest extends Request<GetPracticeListResponse> {
  constructor(
    public readonly start: number,
    public readonly limit: number
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
export class SearchPracticeRequest extends Request<SearchPracticeResponse> {
  constructor(
    public readonly like: string,
    public readonly start: number,
    public readonly limit: number,
  ) {
    super('GET', '/practice/searchList', true);
  }
}

export type SearchPracticeResponse = Response<SimplePracticeInfo[]>

/**
Get pratice detail

POST /practice/full/get
*/
export class GetPracticeDetailRequest extends Request<GetPracticeDetailResponse> {
  constructor(
    public readonly qname: string
  ) {
    super('GET', '/practice/full/get', true);
  }
}

export type GetPracticeDetailResponse = Response<PraciceInfo>

/**
Submit code

POST /practice/submit
*/
export class SubmitPracticeRequest extends Request<SubmitPracticeResponse> {
  constructor(
    public readonly qname: string,
    public readonly lang: string,
    public readonly code: string
  ) {
    super('POST', '/practice/submit', true);
  }

  protected getHeaders(): AxiosRequestConfig<object | string>['headers'] {
    return {
      ...super.getHeaders(),
      'Content-Type': 'application/plain'
    }
  }

  protected getBody(): string | object | undefined {
    return this.code;
  }

  protected getParams(): object | undefined {
    return {
      qname: this.qname,
      lang: this.lang
    }
  }
}

// 0: accepted, 1: wrong answer, 2: compile message, 3: runtime message, 4: time limit exceeded, 5: server message
export type SubmitPracticeResponse = Response<undefined>
