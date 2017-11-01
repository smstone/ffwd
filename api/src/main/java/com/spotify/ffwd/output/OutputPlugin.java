/*
 * Copyright 2013-2017 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.spotify.ffwd.output;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.spotify.ffwd.Plugin;
import com.spotify.ffwd.filter.Filter;
import java.util.Optional;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
public abstract class OutputPlugin extends Plugin {
    
    protected final Optional<Long> flushInterval;
    
    public OutputPlugin() {
        super("", Optional.empty());
        flushInterval = Optional.empty();
    }

    public OutputPlugin(
        final String id, final Optional<Filter> filter, final Optional<Long> flushInterval
    ) {
        super(id, filter);
        this.flushInterval = flushInterval;
    }

    protected Module wrapPluginSink(
        final Key<? extends PluginSink> input, final Key<PluginSink> output
    ) {
        return new PrivateModule() {
            @Override
            protected void configure() {
                Key<PluginSink> sinkKey = (Key<PluginSink>) input;

                if (flushInterval.isPresent() || BatchedPluginSink.class.isAssignableFrom(
                    sinkKey.getTypeLiteral().getRawType())) {
                    final Key<PluginSink> flushingKey = Key.get(PluginSink.class, Names.named("flushing"));
                    install(new OutputDelegatingModule<>(sinkKey, flushingKey,
                        new FlushingPluginSink(flushInterval.get())));
                    sinkKey = flushingKey;
                }

                if (filter.isPresent()) {
                    final Key<PluginSink> filteringKey = Key.get(PluginSink.class, Names.named("filtered"));
                    install(new OutputDelegatingModule<>(sinkKey, filteringKey,
                        new FilteringPluginSink(filter.get())));
                    sinkKey = filteringKey;
                }

                bind(output).to(sinkKey);
                expose(output);
            }
        };
    }

    public abstract Module module(Key<PluginSink> key, String id);
}
