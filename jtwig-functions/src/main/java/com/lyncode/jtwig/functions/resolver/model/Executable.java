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

package com.lyncode.jtwig.functions.resolver.model;

import com.lyncode.jtwig.functions.repository.model.Function;

import java.lang.reflect.InvocationTargetException;

public class Executable {
    private final Function reference;
    private final Object[] arguments;

    public Executable(Function reference, Object[] arguments) {
        this.reference = reference;
        this.arguments = arguments;
    }

    public Object execute () throws InvocationTargetException, IllegalAccessException {
        return reference.method().invoke(reference.holder(), arguments);
    }
}
