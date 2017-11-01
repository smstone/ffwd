package com.spotify.ffwd.output;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import lombok.Data;

@Data
public class OutputDelegatingModule<T extends PluginSink> extends PrivateModule {
    private final Key<PluginSink> input;
    private final Key<PluginSink> output;
    private final T impl;

    @Override
    protected void configure() {
        bind(input.getTypeLiteral()).to(input);
        bind(output).toInstance(impl);
        expose(output);
    }
}
