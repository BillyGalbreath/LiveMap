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
  public static readonly HEADER_SIZE: number = 16;
  public static readonly LONG_BYTES: number = 8;
  public static readonly INT_BYTES: number = 4;

  private readonly _view: DataView;
  private readonly _data: Uint8Array;

  constructor(data: Uint8Array) {
    this._data = data;
    this._view = new DataView(this._data.buffer);
  }

  public getBlock(x: number, z: number): Block {
    const index: number = ((z & 511) << 9) | (x & 511);
    const offset: number = BlockInfo.HEADER_SIZE + index * BlockInfo.LONG_BYTES;
    return new Block(this.mostSigBits(offset), this.leastSigBits(offset), this.minY);
  }

  public mostSigBits(offset: number): number {
    return this._view.getUint32(offset);
  }

  public leastSigBits(offset: number): number {
    return this._view.getUint32(offset + BlockInfo.INT_BYTES);
  }

  public get minY(): number {
    // see BlockInfoCanvas.java for header structure
    const leastSigBits: number = this.leastSigBits(8);
    const uint24Bits: number = leastSigBits & 0xFFFFFF;

    // shift to top bit, then back to preserve sign
    return (uint24Bits << 8) >> 8;
  }
}
