package com.lyncode.jtwig.unit.services.impl.url;

import com.lyncode.jtwig.services.impl.url.IdentityUrlResolver;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;

public class IdentityUrlResolverTest {
    @Test
    public void outputsTheInput() throws Exception {
        MatcherAssert.assertThat(IdentityUrlResolver.INSTANCE.resolve("", "/test", ""), equalTo("/test"));
    }
}