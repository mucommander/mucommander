/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.functions.postscript;

/**
 * OperatorsNames purpose is to define all operators in the PostScript type 4
 * function definitions. The function also provides an accelerated operand
 * lookup method #getType() which returns an integer representation of the
 * operand so that it can be quickly compared/processed at a later time.
 *
 * @author ICEsoft Technologies Inc.
 * @since 4.2
 */
public class OperatorNames {

    /**
     * Gets the operator type specified by the input substring.  PostScript
     * operands should all be lower case but this function will handle upper
     * and mixed case if encountered.
     *
     * @param ch     character array containing postscript calculator function.
     * @param offset starting offset of input substring.
     * @param length length of input substring.
     * @return integer representing the operand found as described in class
     *         constants.
     */
    public static int getType(char ch[], int offset, int length) {
        char c1, c2;
        char c = ch[offset];
        // quickly switch though possible operands to find matching operands
        // as quickly as possible.
        switch (c) {
            case 'a': // abs | add | atan
            case 'A':
                if (length == 4) return OP_ATAN;
                c1 = ch[offset + 1];
                if (c1 == 'b' || c1 == 'B') {
                    return OP_ABS;
                } else if (c1 == 'd' || c1 == 'D') {
                    return OP_ADD;
                }
                break;
            case 'b': // bitshift
            case 'B':
                return OP_BITSHIFT;
            case 'c': // ceiling | cos | copy | cvi | cvr
            case 'C':
                if (length == 8) return OP_CEILING;
                if (length == 4) return OP_COPY;
                c1 = ch[offset + 1];
                if (c1 == 'o' || c1 == 'O') {
                    return OP_COS;
                } else if (c1 == 'v' || c1 == 'V') {
                    c2 = ch[offset + 2];
                    if (c2 == 'i' || c2 == 'I') {
                        return OP_CVI;
                    } else if (c2 == 'r' || c2 == 'R') {
                        return OP_CVR;
                    }
                }
                break;
            case 'd': // div | dup
            case 'D':
                c1 = ch[offset + 1];
                if (c1 == 'i' || c1 == 'I') {
                    return OP_DIV;
                } else if (c1 == 'u' || c1 == 'U') {
                    return OP_DUP;
                }
                break;
            case 'e': // eq | exch | exp
            case 'E':
                if (length == 2) return OP_EQ;
                if (length == 3) return OP_EXP;
                if (length == 4) return OP_EXCH;
                break;
            case 'f': // floor
            case 'F':
                return OP_FLOOR;
            case 'g': // div | dup
            case 'G':
                c1 = ch[offset + 1];
                if (c1 == 'e' || c1 == 'E') {
                    return OP_GE;
                } else if (c1 == 't' || c1 == 'T') {
                    return OP_GT;
                }
                break;
            case 'i': // idif | if | ifelse | in | index
            case 'I':
                if (length == 6) return OP_IFELSE;
                c1 = ch[offset + 1];
                if (c1 == 'd' || c1 == 'D') {
                    return OP_IDIV;
                } else if (c1 == 'f' || c1 == 'F') {
                    return OP_IF;
                } else if (c1 == 'n' || c1 == 'N') {
                    if (length == 5) return OP_INDEX;
                    if (length == 2) return OP_LN;
                }
                break;
            case 'l': // le | log | lt
            case 'L':
                if (length == 3) return OP_LOG;
                c1 = ch[offset + 1];
                if (c1 == 'e' || c1 == 'E') {
                    return OP_LE;
                } else if (c1 == 't' || c1 == 'T') {
                    return OP_LT;
                }
                break;
            case 'm': // mod | mul
            case 'M':
                c1 = ch[offset + 1];
                if (c1 == 'o' || c1 == 'O') {
                    return OP_MOD;
                } else if (c1 == 'u' || c1 == 'U') {
                    return OP_MUL;
                }
                break;
            case 'n': // ne | neg | not
            case 'N':
                if (length == 2) return OP_NE;
                c1 = ch[offset + 1];
                if (c1 == 'e' || c1 == 'e') {
                    return OP_NEG;
                } else if (c1 == 'o' || c1 == 'O') {
                    return OP_NOT;
                }
                break;
            case 'o': // or
            case 'O':
                return OP_OR;
            case 'p': // pop
            case 'P':
                return OP_POP;
            case 'r': // roll | round
            case 'R':
                if (length == 4) return OP_ROLL;
                if (length == 5) return OP_ROUND;
                break;
            case 's': // sin | sqrt | sub
            case 'S':
                if (length == 4) return OP_SQRT;
                c1 = ch[offset + 1];
                if (c1 == 'u' || c1 == 'U') {
                    return OP_SUB;
                } else if (c1 == 'i' || c1 == 'I') {
                    return OP_SIN;
                }
                break;
            case 't': // truncate
            case 'T':
                return OP_TRUNCATE;
            case 'x': // xor
            case 'X':
                return OP_XOR;
            case '{':
                return OP_EXP_START;
            case '}':
                return OP_EXP_END;
        }
        return NO_OP;
    }

    /**
     * Postscript subset of operations used in a type 4 function PostScript
     * calculator.
     */
    public final static int
            NO_OP = 0,
            OP_ABS = 1,
            OP_ADD = 2,
            OP_AND = 3,
            OP_ATAN = 4,
            OP_BITSHIFT = 5,
            OP_CEILING = 6,
            OP_COS = 7,
            OP_COPY = 8,
            OP_CVI = 9,
            OP_CVR = 10,
            OP_DIV = 11,
            OP_DUP = 12,
            OP_EQ = 13,
            OP_EXCH = 14,
            OP_EXP = 15,
            OP_FALSE = 16,
            OP_FLOOR = 17,
            OP_GE = 18,
            OP_GT = 19,
            OP_IDIV = 20,
            OP_IF = 21,
            OP_IFELSE = 22,
            OP_LN = 23,
            OP_INDEX = 24,
            OP_LE = 25,
            OP_LOG = 26,
            OP_LT = 27,
            OP_MOD = 28,
            OP_MUL = 29,
            OP_NE = 30,
            OP_NEG = 31,
            OP_NOT = 32,
            OP_OR = 33,
            OP_POP = 34,
            OP_ROLL = 35,
            OP_ROUND = 36,
            OP_SIN = 37,
            OP_SQRT = 38,
            OP_SUB = 39,
            OP_TRUE = 40,
            OP_TRUNCATE = 41,
            OP_XOR = 42,
            OP_EXP_START = 43,
            OP_EXP_END = 44,
            OP_PROC = 45;
}
