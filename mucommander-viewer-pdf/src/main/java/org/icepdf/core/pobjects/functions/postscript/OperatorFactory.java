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

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Operator factory takes a operand char offset and quickly returns a Operator
 * object which contains the respective operator evaluation logic.   The calling
 * method can defer the execution of operator as needed.
 *
 * @author ICEsoft Technologies Inc.
 * @since 4.2
 */
public class OperatorFactory {

    private static ConcurrentHashMap<Integer, Operator> operatorCache =
            new ConcurrentHashMap<Integer, Operator>();

    @SuppressWarnings(value = "unchecked")
    public static Operator getOperator(char ch[], int offset, int length) {

        // get the operator int value.
        final int operatorType = OperatorNames.getType(ch, offset, length);

        // check operator cache
        Operator operator = operatorCache.get(operatorType);
        if (operator != null) {
            return operator;
        }
        // build the operation, consider added a few if range checks to limit
        // the number of compares.
        switch (operatorType) {
            /**
             * num1 abs =  num2
             *    4.5 abs -> 4.5
             *     3 abs -> 3
             *     0 abs -> 0
             */
            case OperatorNames.OP_ABS:
                operator = new Operator(OperatorNames.OP_ABS) {
                    public void eval(Stack stack) {
                        Float num = (Float) stack.pop();
                        stack.push(Math.abs(num));
                    }
                };
                break;
            /**
             * num1 num2 add = sum
             *      3 4 add -> 7
             *  9.9 1.1 add -> 11.0
             */
            case OperatorNames.OP_ADD:
                operator = new Operator(OperatorNames.OP_ADD) {
                    public void eval(Stack stack) {
                        Float num2 = (Float) stack.pop();
                        Float num1 = (Float) stack.pop();
                        stack.push(num1 + num2);
                    }
                };
                break;
            /**
             *  bool1 bool2 and  = bool3
             *  int1 int2 and  = int3
             *      true true and -> true % A complete truth table
             *     true false and -> false
             *     false true and -> false
             *    false false and -> false
             *           99 1 and -> 1
             *           52 7 and -> 4
             */
            case OperatorNames.OP_AND:
                operator = new Operator(OperatorNames.OP_AND) {
                    public void eval(Stack stack) {
                        Object value = stack.pop();
                        if (value instanceof Boolean) {
                            boolean bool2 = (Boolean) value;
                            boolean bool1 = (Boolean) stack.pop();
                            stack.push(bool1 && bool2);
                        } else {
                            int val1 = ((Float) value).intValue();
                            int val2 = ((Float) stack.pop()).intValue();
                            stack.push(val1 & val2);
                        }
                    }
                };
                break;
            /***
             *  num den atan = angle
             *      0 1 atan -> 0.0
             *      1 0 atan -> 90.0
             *   -100 0 atan -> 270.0
             *      4 4 atan -> 45.0
             */
            case OperatorNames.OP_ATAN:
                operator = new Operator(OperatorNames.OP_ATAN) {
                    public void eval(Stack stack) {
                        float den = (Float) stack.pop();
                        float num = (Float) stack.pop();
                        stack.push(((Number) Math.toDegrees(Math.atan(num / den))).floatValue());
                    }
                };
                break;
            /***
             *  int1 shift bitshift int2
             *      07 3 bitshift -> 56
             *    142 3 bitshift -> 17
             */
            case OperatorNames.OP_BITSHIFT:
                operator = new Operator(OperatorNames.OP_BITSHIFT) {
                    public void eval(Stack stack) {
                        long shift = (Long) stack.pop();
                        long int1 = (Long) stack.pop();
                        stack.push(int1 << shift);
                    }
                };
                break;
            /**
             * num1 ceiling = num2
             *    3.2 ceiling -> 4.0
             *   4.8 ceiling -> 4.0
             *     99 ceiling -> 99
             */
            case OperatorNames.OP_CEILING:
                operator = new Operator(OperatorNames.OP_CEILING) {
                    public void eval(Stack stack) {
                        float num1 = (Float) stack.pop();
                        stack.push(((Number) Math.ceil(num1)).floatValue());
                    }
                };
                break;
            /**
             *  aAngle cos = real
             *    0 cos -> 1.0
             *    90 cos -> 0.0
             */
            case OperatorNames.OP_COS:
                operator = new Operator(OperatorNames.OP_COS) {
                    public void eval(Stack stack) {
                        float aAngle = (Float) stack.pop();
                        stack.push(((Number) Math.cos(aAngle)).floatValue());
                    }
                };
                break;
            /**
             * any1 ... anyn n copy any1 ... anyn any1 ... anyn
             *
             *         array1 array2 copy subarray2
             *           dict1 dict2 copy dict2
             *       string1 string2 copy substring2
             *   packedarray1 array2 copy subarray2
             *       gstate1 gstate2 copy gstate2
             *
             *    (a) (b) (c) 2 copy -> (a) (b) (c) (b) (c)
             *    (a) (b) (c) 0 copy -> (a) (b) (c)
             */
            case OperatorNames.OP_COPY:
                operator = new Operator(OperatorNames.OP_COPY) {
                    public void eval(Stack stack) {
                        int n = ((Float) stack.pop()).intValue();
                        int top = stack.size();
                        for (int i = top - n; i < top; i++) {
                            stack.push(stack.get(i));
                        }
                    }
                };
                break;
            /**
             * num cvi  = int
             * string cvi = int
             *
             *    (3.3E1) cvi -> 33
             *      47.8 cvi -> 47
             *      520.9 cvi -> 520
             */
            case OperatorNames.OP_CVI:
                operator = new Operator(OperatorNames.OP_CVI) {
                    public void eval(Stack stack) {
                        // doesn't really convert to int but not a bit deal for
                        // java in general.
                        int number = ((Float) stack.pop()).intValue();
                        stack.push(number);
                    }
                };
                break;
            /**
             * num cvr real
             * string cvr real
             */
            case OperatorNames.OP_CVR:
                operator = new Operator(OperatorNames.OP_CVR) {
                    public void eval(Stack stack) {
                        // doesn't really convert to int but not a bit deal for
                        // java in general.
                        float number = (Float) stack.pop();
                        stack.push(number);
                    }
                };
                break;
            /**
             *  num1 num2 div quotient
             *    3 2 div -> 1.5
             *    4 2 div -> 2.0
             */
            case OperatorNames.OP_DIV:
                operator = new Operator(OperatorNames.OP_DIV) {
                    public void eval(Stack stack) {
                        // doesn't really convert to int but not a bit deal for
                        // java in general.
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 / num2);
                    }
                };
                break;
            /**
             * any dup = any any
             * duplicates the top element on the operand stack
             */
            case OperatorNames.OP_DUP:
                operator = new Operator(OperatorNames.OP_DUP) {
                    public void eval(Stack stack) {
                        // peek and push should give us the duplication.
                        stack.push(stack.peek());
                    }
                };
                break;
            /**
             * any1 any2 eq bool
             * pops two objects from the operand stack and pushes true if they
             * are equal, or false if not.
             */
            case OperatorNames.OP_EQ:
                operator = new Operator(OperatorNames.OP_EQ) {
                    public void eval(Stack stack) {
                        Object any2 = stack.pop();
                        Object any1 = stack.pop();
                        stack.push(any1.equals(any2));
                    }
                };
                break;
            /**
             * any1 any2 exch any2 any1
             * exchanges the top two elements on the operand stack.
             *   1 2 exch -> 2 1
             */
            case OperatorNames.OP_EXCH:
                operator = new Operator(OperatorNames.OP_EXCH) {
                    public void eval(Stack stack) {
                        Object any2 = stack.pop();
                        Object any1 = stack.pop();
                        stack.push(any2);
                        stack.push(any1);
                    }
                };
                break;
            /**
             * base exponent exp = real
             *   9 0.5 exp -> 3.0
             *   -9 -1 exp -> -0.111111
             */
            case OperatorNames.OP_EXP:
                operator = new Operator(OperatorNames.OP_EXP) {
                    public void eval(Stack stack) {
                        float exponent = (Float) stack.pop();
                        float base = (Float) stack.pop();
                        stack.push(((Number) Math.pow(base, exponent)).floatValue());
                    }
                };
                break;
            /**
             * num1 floor num2
             *      3.2 floor -> 3.0
             *     -4.8 floor -> -5.0
             *       99 floor -> 99
             */
            case OperatorNames.OP_FLOOR:
                operator = new Operator(OperatorNames.OP_FLOOR) {
                    public void eval(Stack stack) {
                        float num1 = (Float) stack.pop();
                        stack.push(((Number) Math.floor(num1)).floatValue());
                    }
                };
                break;
            /**
             *  num1 num2 ge bool
             *  string1 string2 ge bool
             *
             *  pops two objects from the operand stack and pushes true if the
             *  first operand iS greater than or equal to the second, or false
             *  otherwise.
             *       4.2 4 ge -> true
             *      (abc) (d) ge -> false
             *      (aba) (ab) ge -> true
             *      (aba) (aba) ge -> true
             */
            case OperatorNames.OP_GE:
                operator = new Operator(OperatorNames.OP_GE) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 >= num2);
                    }
                };
                break;
            /**
             *  num1 num2 gt bool
             * string1 string2 gt bool (not implemented)
             */
            case OperatorNames.OP_GT:
                operator = new Operator(OperatorNames.OP_GT) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 > num2);
                    }
                };
                break;
            /**
             *  int1 int2 idiv quotient
             *
             *  divides int1 by int2 and returns the integer part of the quotient,
             *  with any fractional part discarded. Both operands of idiv must
             *  be integers and the result is an integer.
             *      Examples
             *          3 2 idiv -> 1
             *          4 2 idiv -> 2
             *          -5 2 idiv -> -2
             */
            case OperatorNames.OP_IDIV:
                operator = new Operator(OperatorNames.OP_IDIV) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push((int) (num1 / num2));
                    }
                };
                break;
            /**
             * bool expression if
             * removes both operands from the stack, then executes proc if bool is true.
             *
             * 3 4 lt {(3 is less than 4)} if -> (3 is less than 4)
             */
            case OperatorNames.OP_IF:
                operator = new Operator(OperatorNames.OP_IF) {
                    public void eval(Stack stack) {
                        // pop off the express so we can get at the bool
                        // if we don't have an Expression we can't continue.
                        Procedure proc1 = null;
                        if (stack.peek() instanceof Procedure) {
                            proc1 = (Procedure) stack.pop();
                        }
                        boolean bool = (Boolean) stack.pop();
                        // process expression 'if' expression is true
                        if (bool) {
                            proc1.eval(stack);
                        }
                    }
                };
                break;
            /**
             * bool proc1 proc2 ifelse -
             * removes all three operands from the stack, then executes proc1
             * if bool is true or proc2 if bool is false.
             *
             * 3 4 lt {(3 is less than 4)} if -> (3 is less than 4)
             */
            case OperatorNames.OP_IFELSE:
                operator = new Operator(OperatorNames.OP_IFELSE) {
                    public void eval(Stack stack) {
                        // if we don't have an Expression we can't continue.
                        Procedure proc2 = null, proc1 = null;
                        if (stack.peek() instanceof Procedure) {
                            proc2 = (Procedure) stack.pop();
                        }
                        if (stack.peek() instanceof Procedure) {
                            proc1 = (Procedure) stack.pop();
                        }
                        boolean bool = (Boolean) stack.pop();
                        // process ifelse clause
                        if (bool) {
                            proc1.eval(stack);
                        } else {
                            proc2.eval(stack);
                        }
                    }
                };
                break;
            /**
             * anyn ... any0 n index anyn ... any0 anyn
             *
             * removes the nonnegative integer n from the operand stack, counts
             * down to the nth element from the top of the stack, and pushes a
             * copy of that element on the stack.
             *           (a) (b) (c) (d) 0 index -> (a) (b) (c) (d) (d)
             *           (a) (b) (c) (d) 3 index -> (a) (b) (c) (d) (a)
             */
            case OperatorNames.OP_INDEX:
                operator = new Operator(OperatorNames.OP_INDEX) {
                    public void eval(Stack stack) {
                        float n = (Float) stack.pop();
                        stack.push(stack.get((int) ((stack.size() - 1) - n)));
                    }
                };
                break;
            /**
             * num1 num2 le bool
             * string1 string2 le bool
             *
             * pops two objects from the operand stack and pushes true if the
             * first operand is less than or equal to the second, or false
             * otherwise.
             */
            case OperatorNames.OP_LE:
                operator = new Operator(OperatorNames.OP_LE) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 <= num2);
                    }
                };
                break;
            /**
             * num ln real
             * returns the natural logarithm (base e) of num.
             */
            case OperatorNames.OP_LN:
                operator = new Operator(OperatorNames.OP_LN) {
                    public void eval(Stack stack) {
                        float num = (Float) stack.pop();
                        stack.push(((Number) Math.log(num)).floatValue());
                    }
                };
                break;
            /**
             * num log real
             * returns the common logarithm (base 10) of num.
             */
            case OperatorNames.OP_LOG:
                operator = new Operator(OperatorNames.OP_LOG) {
                    public void eval(Stack stack) {
                        float num = (Float) stack.pop();
                        stack.push(((Number) Math.log10(num)).floatValue());
                    }
                };
                break;
            /**
             *  num1 num2 lt bool
             */
            case OperatorNames.OP_LT:
                operator = new Operator(OperatorNames.OP_LT) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 < num2);
                    }
                };
                break;
            /**
             * int1 int2 mod remainder
             * returns the remainder that results from dividing int1 by int2.
             */
            case OperatorNames.OP_MOD:
                operator = new Operator(OperatorNames.OP_MOD) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 % num2);
                    }
                };
                break;
            /**
             * num1 num2 mul product
             * returns the product of num1 and num2.
             */
            case OperatorNames.OP_MUL:
                operator = new Operator(OperatorNames.OP_MUL) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 * num2);
                    }
                };
                break;
            /**
             * any1 any2 ne bool
             * pops two objects from the operand stack and pushes false if they
             * are equal, or true if not.
             */
            case OperatorNames.OP_NE:
                operator = new Operator(OperatorNames.OP_NE) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 != num2);
                    }
                };
                break;
            /**
             * num1 neg num2
             * returns the negative of num1.
             */
            case OperatorNames.OP_NEG:
                operator = new Operator(OperatorNames.OP_NEG) {
                    public void eval(Stack stack) {
                        float num1 = (Float) stack.pop();
                        stack.push(-num1);
                    }
                };
                break;
            /**
             * bool1 not bool2
             * returns the logical negation of the operand if it is boolean
             */
            case OperatorNames.OP_NOT:
                operator = new Operator(OperatorNames.OP_NOT) {
                    public void eval(Stack stack) {
                        boolean num1 = (Boolean) stack.pop();
                        stack.push(!num1);
                    }
                };
                break;
            /**
             * bool1 bool2 or bool3
             * returns the logical disjunction of the operands if they are boolean.
             */
            case OperatorNames.OP_OR:
                operator = new Operator(OperatorNames.OP_OR) {
                    public void eval(Stack stack) {
                        boolean bool2 = (Boolean) stack.pop();
                        boolean bool1 = (Boolean) stack.pop();
                        stack.push(bool1 || bool2);
                    }
                };
                break;
            /**
             * any pop
             * removes the top element from the operand stack and discards it.
             */
            case OperatorNames.OP_POP:
                operator = new Operator(OperatorNames.OP_POP) {
                    public void eval(Stack stack) {
                        stack.pop();
                    }
                };
                break;
            /**
             * anyn-1 ... any0 n j roll any (j-1) mod n ... any0 anyn-1 ... anyj mod n
             *
             * performs a circular shift of the objects anyn-1 through any0 on
             * the operand stack by the amount j. Positive j indicates upward
             * motion on the stack, whereas negative j indicates downward motion.
             * n must be a nonnegative integer and j must be an integer. roll
             * first removes these operands from the stack; there must be at
             * least n additional elements. It then performs a circular shift
             * of these n elements by j positions. If j is positive, each shift
             * consists of removing an element from the top of the stack and
             * inserting it between element n - 1 and element n of the stack,
             * moving all in tervening elements one level higher on the stack.
             * If j is negative, each shift consists of removing element n - 1
             * of the stack and pushing it on the top of the stack, moving all
             * intervening elements one level lower on the stack.
             *
             *    (a) (b) (c) 3 -1 roll -> (b) (c) (a)
             *     (a) (b) (c) 3 1 roll -> (c) (a) (b)
             *     (a) (b) (c) 3 0 roll -> (a) (b) (c)
             */
            case OperatorNames.OP_ROLL:
                operator = new Operator(OperatorNames.OP_ROLL) {
                    public void eval(Stack stack) {
                        float j = (Float) stack.pop();
                        float n = (Float) stack.pop();
                        // each sift consists of removing an element from the top of the
                        // stack and inserting it between element n-1 and element n of the stack
                        if (j > 0) {
                            for (int i = 0; i < j; i++) {
                                stack.insertElementAt(stack.lastElement(),
                                        (int) (stack.size() - (n)));
                                // finish the move by poping the top;
                                stack.pop();
                            }
                        }
                        // each shift consists of removing an element n-1 off the stack
                        // and pushing it on top of the stack
                        else if (j < 0) {
                            for (int i = 0, max = (int) -j; i < max; i++) {
                                stack.push(stack.remove((int) (stack.size() - (n))));
                            }
                        }
                    }
                };
                break;
            /**
             * num1 round num2
             * returns the integer value nearest to num1
             */
            case OperatorNames.OP_ROUND:
                operator = new Operator(OperatorNames.OP_ROUND) {
                    public void eval(Stack stack) {
                        float num1 = (Float) stack.pop();
                        stack.push(((Number) Math.round(num1)).floatValue());
                    }
                };
                break;
            /**
             *  angle sin real
             *  returns the sine of angle, which is interpreted as an angle in degrees.
             */
            case OperatorNames.OP_SIN:
                operator = new Operator(OperatorNames.OP_SIN) {
                    public void eval(Stack stack) {
                        float aAngle = (Float) stack.pop();
                        stack.push(((Number) Math.sin(aAngle)).floatValue());
                    }
                };
                break;
            /**
             *  num sqrt real
             *  returns the sine of angle, which is interpreted as an angle in degrees.
             */
            case OperatorNames.OP_SQRT:
                operator = new Operator(OperatorNames.OP_SQRT) {
                    public void eval(Stack stack) {
                        float num = (Float) stack.pop();
                        stack.push(((Number) Math.sqrt(num)).floatValue());
                    }
                };
                break;
            /**
             * num1 num2 sub difference
             * returns the result of subtracting num2 from num1.
             */
            case OperatorNames.OP_SUB:
                operator = new Operator(OperatorNames.OP_SUB) {
                    public void eval(Stack stack) {
                        float num2 = (Float) stack.pop();
                        float num1 = (Float) stack.pop();
                        stack.push(num1 - num2);
                    }
                };
                break;
            /**
             * num1 truncate num2
             * truncates num1 toward 0 by removing its fractional part.
             */
            case OperatorNames.OP_TRUNCATE:
                operator = new Operator(OperatorNames.OP_TRUNCATE) {
                    public void eval(Stack stack) {
                        float num1 = (Float) stack.pop();
                        stack.push(((Number) Math.floor(num1)).floatValue());
                    }
                };
                break;
            /**
             * bool1 bool2 xor bool3
             * int1 int2 xor int3
             * returns the logical "exclusive or" of the operands if they are
             * boolean. If the operands are integers, xor returns the bitwise
             * "exclusive or" of their binary representations.
             */
            case OperatorNames.OP_XOR:
                operator = new Operator(OperatorNames.OP_XOR) {
                    public void eval(Stack stack) {
                        Object obj2 = stack.pop();
                        if (obj2 instanceof Number) {
                            float num2 = (Float) obj2;
                            float num1 = (Float) stack.pop();
                            stack.push((int) num1 ^ (int) num2);
                        } else if (obj2 instanceof Boolean) {
                            boolean bool2 = (Boolean) obj2;
                            boolean bool1 = (Boolean) stack.pop();
                            stack.push(bool1 ^ bool2);
                        }
                    }
                };
                break;
            case OperatorNames.OP_EXP_START:
                operator = new Expression(OperatorNames.OP_EXP_START);
                break;
            case OperatorNames.OP_EXP_END:
                operator = new Expression(OperatorNames.OP_EXP_END);
                break;
            default:
                operator = new Operator(OperatorNames.NO_OP) {
                    public void eval(Stack stack) {
                        // throw something?
                        System.out.println(operatorType + " not implemented ");
                    }
                };
                break;
        }

        // add the new operator to the cache
        if (operator != null) {
            operatorCache.put(operator.getType(), operator);
        }
        return operator;
    }
}
