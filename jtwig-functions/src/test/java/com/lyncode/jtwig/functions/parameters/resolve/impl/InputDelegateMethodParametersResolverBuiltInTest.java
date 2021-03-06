package com.lyncode.jtwig.functions.parameters.resolve.impl;

import com.lyncode.jtwig.functions.builtin.ListFunctions;
import com.lyncode.jtwig.functions.parameters.convert.DemultiplexerConverter;
import com.lyncode.jtwig.functions.parameters.input.InputParameters;
import com.lyncode.jtwig.functions.parameters.resolve.api.InputParameterResolverFactory;
import com.lyncode.jtwig.functions.parameters.resolve.api.MethodParametersResolver;
import com.lyncode.jtwig.functions.parameters.resolve.api.ParameterResolver;
import com.lyncode.jtwig.functions.parameters.resolve.model.ResolvedParameters;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.lyncode.jtwig.functions.parameters.input.InputParameters.parameters;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

public class InputDelegateMethodParametersResolverBuiltInTest {
    private InputParameterResolverFactory resolverFactory = new InputParameterResolverFactory() {
        @Override
        public ParameterResolver create(InputParameters parameters) {
            return new ParameterAnnotationParameterResolver(parameters, new DemultiplexerConverter());
        }
    };
    private MethodParametersResolver underTest = new InputDelegateMethodParametersResolver(resolverFactory);

    @Test
    public void canResolveParameters() throws Exception {
        ResolvedParameters resolve = underTest.resolve(new ResolvedParameters(batchFunction()), parameters(asList(1, 2, 3, 4), 3));
        assertTrue(resolve.isFullyResolved());
    }

    private Method batchFunction() {
        try {
            return ListFunctions.class.getDeclaredMethod("batch", Object.class, int.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
