import type { PraciceInfo, SimplePracticeInfo } from '@/models/Practice'
import { Request, type Response } from './Api'
import { getDecodedJwt } from '@/persistence/LocalPersistence';

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
export class SearchPracticeRequest extends Request<SearchPracticeRequest> {
  constructor(
    public readonly start: number,
    public readonly limit: number,
    public readonly order: string,
    public readonly like: string
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
    public readonly name: string
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
    public readonly code: string
  ) {
    super('POST', '/practice/submit', true);
  }

  // TODO: to be fixed
  protected getParams(): object | undefined {
    const userInfo = getDecodedJwt();
    if (userInfo === null) throw new Error('No')
    return {
      name: userInfo.name,
      cnname: userInfo.cnname
    }
  }
}

// 0: accepted, 1: wrong answer, 2: compile error, 3: runtime error, 4: time limit exceeded, 5: server error
export type SubmitPracticeResponse = Response<undefined>
