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

/**
 * Procedure represents a groups of operands enclosed by braces.  For example
 * {dup 0 lt {pop 0 }{dup 1 gt {pop 1 } if } ifelse  is defined as:
 * {operand... {proc} {proc {proc}}operand} operand}
 * <p/>
 * This structures aids in the handling of ifelse and if statements.
 *
 * @since 5.1.0
 */
public class Procedure extends Operator {

    private Stack<Object> stack;
    private Procedure previousProcedure;

    @SuppressWarnings("unchecked")
    public Procedure(Procedure previousProcedure) {
        super(OperatorNames.OP_PROC);
        stack = new Stack<Object>();
        if (previousProcedure != null) {
            previousProcedure.getProc().push(this);
        }
        this.previousProcedure = previousProcedure;
    }

    public Procedure getPrevious() {
        return previousProcedure;
    }

    public Stack<Object> getProc() {
        return stack;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void eval(Stack stack) {
        // iterate over the stack objects and update the eval stack
        // we need to to this in revers...
        for (Object tmp : this.stack) {
            if (tmp instanceof Operator && !(tmp instanceof Procedure)) {
                ((Operator) tmp).eval(stack);
            } else {
                stack.push(tmp);
            }
        }
    }
}
