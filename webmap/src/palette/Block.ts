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

export class Block {
  private readonly _block: number;
  private readonly _biome: number;
  private readonly _yPos: number;
  private readonly _minY: number;

  constructor(packed: number, minY: number) {
    // 11111111111111111111111111111111 - 32 bits - (4294967295)
    // 11111111111                      - 11 bits - block (2047)
    //            111111111             -  9 bits - biome (511)
    //                     111111111111 - 12 bits - yPos  (4095)
    this._block = packed >>> 21;
    this._biome = (packed & 0b00000000000_111111111_000000000000) >>> 12;
    this._yPos = packed & 0b00000000000_000000000_111111111111;
    this._minY = minY;
  }

  get block(): number {
    return this._block;
  }

  get biome(): number {
    return this._biome;
  }

  get yPos(): number {
    return this._yPos + this._minY;
  }
}
