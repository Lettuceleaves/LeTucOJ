export interface SimplePracticeInfo {
  name: string
  cnname: string
}

export interface PraciceInfo extends SimplePracticeInfo {
  name: string
  cnname: string
  caseAmount: number
  difficulty: number
  tags: string
  authors: string
  createtime: Date
  updateat: Date
  content: string
  freq: number // float
  ispublic: boolean
  solution: string
  showsolution: boolean
}
