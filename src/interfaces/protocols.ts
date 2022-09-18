/**
 * @File   : protocols.ts
 * @Author : dtysky (dtysky@outlook.com)
 * @Link   : dtysky.moe
 * @Date   : 2022/9/13 23:44:34
 */
export type TBookType = 'Page' | 'Position';

export interface IBook {
  hash: string;
  type: TBookType;
  name: string;
  author: string;
  cover?: string;
  filePath: string;
  progress: number;
}

// highlights and annotations
export interface IBookNote {
  page: number;
  start: number;
  length: number;
  annotation: string;
}
