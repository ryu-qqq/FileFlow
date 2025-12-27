package com.ryuqq.fileflow.sdk.auth;

/**
 * ThreadLocal holder for FileFlow authentication tokens.
 *
 * <p>This class provides a convenient way to propagate tokens across method calls within the same
 * thread. Typically used in web applications where the token is extracted from the incoming request
 * and needs to be passed to the SDK.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // In a filter or interceptor
 * FileFlowTokenHolder.set(extractedToken);
 * try {
 *     // Call service methods that use FileFlowClient
 *     fileFlowClient.fileAssets().generateDownloadUrl(id);
 * } finally {
 *     FileFlowTokenHolder.clear();
 * }
 * }</pre>
 *
 * <p><strong>Important:</strong> Always call {@link #clear()} in a finally block to prevent memory
 * leaks in thread pools.
 */
public final class FileFlowTokenHolder {

    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    private FileFlowTokenHolder() {
        // Utility class
    }

    /**
     * Sets the token for the current thread.
     *
     * @param token the authentication token (with or without "Bearer " prefix)
     */
    public static void set(String token) {
        TOKEN_HOLDER.set(token);
    }

    /**
     * Gets the token for the current thread.
     *
     * @return the token, or null if not set
     */
    public static String get() {
        return TOKEN_HOLDER.get();
    }

    /**
     * Clears the token for the current thread.
     *
     * <p>Should always be called in a finally block to prevent memory leaks.
     */
    public static void clear() {
        TOKEN_HOLDER.remove();
    }

    /**
     * Checks if a token is set for the current thread.
     *
     * @return true if a token is set, false otherwise
     */
    public static boolean isPresent() {
        return TOKEN_HOLDER.get() != null;
    }
}
