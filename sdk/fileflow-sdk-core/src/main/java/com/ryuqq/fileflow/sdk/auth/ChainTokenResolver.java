package com.ryuqq.fileflow.sdk.auth;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * TokenResolver that chains multiple resolvers and returns the first resolved token.
 *
 * <p>Resolvers are tried in order, and the first one that returns a non-empty Optional is used.
 * This enables priority-based token resolution.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // ThreadLocal first, then static token as fallback
 * TokenResolver resolver = ChainTokenResolver.of(
 *     new ThreadLocalTokenResolver(),
 *     new StaticTokenResolver(serviceToken)
 * );
 * }</pre>
 */
public final class ChainTokenResolver implements TokenResolver {

    private final List<TokenResolver> resolvers;

    private ChainTokenResolver(List<TokenResolver> resolvers) {
        this.resolvers = resolvers;
    }

    /**
     * Creates a ChainTokenResolver with the given resolvers.
     *
     * @param resolvers the resolvers to chain (tried in order)
     * @return a new ChainTokenResolver
     * @throws NullPointerException if resolvers is null
     * @throws IllegalArgumentException if resolvers is empty
     */
    public static ChainTokenResolver of(TokenResolver... resolvers) {
        Objects.requireNonNull(resolvers, "resolvers must not be null");
        if (resolvers.length == 0) {
            throw new IllegalArgumentException("At least one resolver is required");
        }
        return new ChainTokenResolver(Arrays.asList(resolvers));
    }

    /**
     * Creates a ChainTokenResolver with the given resolvers.
     *
     * @param resolvers the resolvers to chain (tried in order)
     * @return a new ChainTokenResolver
     * @throws NullPointerException if resolvers is null
     * @throws IllegalArgumentException if resolvers is empty
     */
    public static ChainTokenResolver of(List<TokenResolver> resolvers) {
        Objects.requireNonNull(resolvers, "resolvers must not be null");
        if (resolvers.isEmpty()) {
            throw new IllegalArgumentException("At least one resolver is required");
        }
        return new ChainTokenResolver(List.copyOf(resolvers));
    }

    /**
     * Creates a default chain resolver with ThreadLocal first, then static token.
     *
     * <p>This is the recommended configuration for most use cases:
     *
     * <ol>
     *   <li>Try ThreadLocal (user's token propagated from request)
     *   <li>Fall back to service token (for background jobs, etc.)
     * </ol>
     *
     * @param serviceToken the fallback service token
     * @return a ChainTokenResolver with ThreadLocal → Static chain
     */
    public static ChainTokenResolver withFallback(String serviceToken) {
        return of(ThreadLocalTokenResolver.INSTANCE, new StaticTokenResolver(serviceToken));
    }

    @Override
    public Optional<String> resolve() {
        return resolvers.stream()
                .map(TokenResolver::resolve)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    @Override
    public String toString() {
        return "ChainTokenResolver" + resolvers;
    }
}
