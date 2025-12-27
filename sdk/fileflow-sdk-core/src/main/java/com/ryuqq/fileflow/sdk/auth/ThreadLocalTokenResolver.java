package com.ryuqq.fileflow.sdk.auth;

import java.util.Optional;

/**
 * TokenResolver that retrieves tokens from {@link FileFlowTokenHolder}.
 *
 * <p>This resolver is useful when tokens are propagated via ThreadLocal, typically in web
 * applications where the token is extracted from incoming requests.
 */
public final class ThreadLocalTokenResolver implements TokenResolver {

    /** Singleton instance. */
    public static final ThreadLocalTokenResolver INSTANCE = new ThreadLocalTokenResolver();

    private ThreadLocalTokenResolver() {
        // Singleton
    }

    @Override
    public Optional<String> resolve() {
        return Optional.ofNullable(FileFlowTokenHolder.get());
    }

    @Override
    public String toString() {
        return "ThreadLocalTokenResolver";
    }
}
