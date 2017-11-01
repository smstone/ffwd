package com.spotify.ffwd;

import com.spotify.ffwd.filter.Filter;
import com.spotify.ffwd.filter.TrueFilter;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public abstract class Plugin {
    protected final String id;
    protected final Optional<Filter> filter;

    protected Plugin(final String id, final Optional<Filter> filter) {
        this.id = id;
        this.filter = filter.isPresent() ? filter : Optional.of(new TrueFilter());
    }

    public String id(int index) {
        return id;
    }

    public Optional<Filter> filter() {
        return filter;
    }
}
