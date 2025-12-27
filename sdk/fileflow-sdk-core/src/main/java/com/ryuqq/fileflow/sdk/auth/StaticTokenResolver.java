package com.ryuqq.fileflow.sdk.auth;

import java.util.Objects;
import java.util.Optional;

/**
 * TokenResolver that always returns a configured static token.
 *
 * <p>This resolver is useful for service-to-service communication where a fixed service token is
 * used for authentication.
 */
public final class StaticTokenResolver implements TokenResolver {

    private final String token;

    /**
     * Creates a new StaticTokenResolver with the given token.
     *
     * @param token the static token to return (must not be null)
     * @throws NullPointerException if token is null
     */
    public StaticTokenResolver(String token) {
        this.token = Objects.requireNonNull(token, "token must not be null");
    }

    @Override
    public Optional<String> resolve() {
        return Optional.of(token);
    }

    @Override
    public String toString() {
        return "StaticTokenResolver[token=***]";
    }
}
