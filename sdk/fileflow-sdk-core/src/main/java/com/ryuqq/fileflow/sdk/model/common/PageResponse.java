package com.ryuqq.fileflow.sdk.model.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Paginated response wrapper.
 *
 * @param <T> the type of items in the page
 */
public final class PageResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    /**
     * Creates a new PageResponse.
     *
     * @param content the items in the current page
     * @param page the current page number (0-based)
     * @param size the page size
     * @param totalElements the total number of elements
     * @param totalPages the total number of pages
     * @param first whether this is the first page
     * @param last whether this is the last page
     */
    @JsonCreator
    public PageResponse(
            @JsonProperty("content") List<T> content,
            @JsonProperty("page") int page,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("first") boolean first,
            @JsonProperty("last") boolean last) {
        this.content = content != null ? new ArrayList<>(content) : new ArrayList<>();
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    public List<T> getContent() {
        return new ArrayList<>(content);
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }
}
