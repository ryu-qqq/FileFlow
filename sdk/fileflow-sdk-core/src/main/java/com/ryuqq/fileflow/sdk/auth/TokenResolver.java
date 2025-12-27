package com.ryuqq.fileflow.sdk.auth;

import java.util.Optional;

/**
 * Strategy interface for resolving authentication tokens.
 *
 * <p>Implementations can retrieve tokens from various sources such as ThreadLocal, static
 * configuration, or security contexts.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * TokenResolver resolver = ChainTokenResolver.of(
 *     new ThreadLocalTokenResolver(),
 *     new StaticTokenResolver("service-token")
 * );
 *
 * Optional<String> token = resolver.resolve();
 * }</pre>
 */
@FunctionalInterface
public interface TokenResolver {

    /**
     * Attempts to resolve an authentication token.
     *
     * @return an Optional containing the token if available, empty otherwise
     */
    Optional<String> resolve();

    /**
     * Returns a TokenResolver that always returns the given token.
     *
     * @param token the static token to return
     * @return a TokenResolver that returns the given token
     */
    static TokenResolver of(String token) {
        return new StaticTokenResolver(token);
    }

    /**
     * Returns a TokenResolver that checks ThreadLocal first.
     *
     * @return a TokenResolver that uses ThreadLocal
     */
    static TokenResolver threadLocal() {
        return ThreadLocalTokenResolver.INSTANCE;
    }

    /**
     * Creates a chain of TokenResolvers that tries each in order.
     *
     * @param resolvers the resolvers to chain
     * @return a ChainTokenResolver
     */
    static TokenResolver chain(TokenResolver... resolvers) {
        return ChainTokenResolver.of(resolvers);
    }
}
