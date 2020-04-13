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
 * Representing a generic operand which follows the Command pattern for delayed
 * or differed execution.
 *
 * @author ICEsoft Technologies Inc.
 * @since 4.2
 */
public abstract class Operator {

    protected int type;

    protected Operator(int type) {
        this.type = type;
    }

    public abstract void eval(Stack stack);

    public boolean equals(Object op) {
        return (op instanceof Operator) && ((Operator) op).type == type;
    }

    public int getType() {
        return type;
    }
}
