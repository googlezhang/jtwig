/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lyncode.jtwig.expressions.operations.factories;

import com.lyncode.jtwig.exception.CompileException;
import com.lyncode.jtwig.expressions.api.BinaryExpressionFactory;
import com.lyncode.jtwig.expressions.api.Expression;
import com.lyncode.jtwig.expressions.model.FunctionElement;
import com.lyncode.jtwig.expressions.model.Variable;
import com.lyncode.jtwig.parser.model.JtwigPosition;

public class CompositionExpressionFactory implements BinaryExpressionFactory {
    @Override
    public Expression expression(JtwigPosition position, Expression left, Expression right) throws CompileException {
        if (right instanceof Variable.Compiled) {
            return function(((Variable.Compiled) right).toFunction(), left);
        } else if (right instanceof FunctionElement.Compiled) {
            return function((FunctionElement.Compiled) right, left);
        } else
            throw new CompileException(position + ": Composition always requires a function to execute as the right argument");
    }

    private Expression function(FunctionElement.Compiled compiled, Expression left) {
        return compiled.cloneAndAddArgument(left);
    }
}
