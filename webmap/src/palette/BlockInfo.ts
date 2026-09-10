/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import {Block} from "./Block";

export class BlockInfo {
  public readonly BYTE_SIZE: number = 8;
  public readonly INTEGER_BYTES: number = 4;

  private readonly _data: Uint8Array;

  constructor(data: Uint8Array) {
    this._data = data;
  }

  get minY(): number {
    return this.getInt(8);
  }

  getBlock(index: number): Block {
    return new Block(this.getInt(12 + index * 4), this.minY);
  }

  private getInt(position: number): number {
    let val: number = 0;
    for (let i: number = 0; i < this.INTEGER_BYTES; i++) {
      val |= (this._data[position + i] & 0xFF) << (this.BYTE_SIZE * ((this.INTEGER_BYTES - 1) - i));
    }
    return val;
  }
}
