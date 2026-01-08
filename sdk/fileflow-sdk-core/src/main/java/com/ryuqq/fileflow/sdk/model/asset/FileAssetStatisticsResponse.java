package com.ryuqq.fileflow.sdk.model.asset;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response for file asset statistics.
 *
 * <p>Contains aggregated counts by status and category.
 */
public final class FileAssetStatisticsResponse {

    private final long totalCount;
    private final Map<String, Long> statusCounts;
    private final Map<String, Long> categoryCounts;

    /**
     * Creates a FileAssetStatisticsResponse.
     *
     * @param totalCount the total number of file assets
     * @param statusCounts counts grouped by status (PENDING, PROCESSING, COMPLETED, FAILED)
     * @param categoryCounts counts grouped by category (IMAGE, VIDEO, DOCUMENT, etc.)
     */
    @JsonCreator
    public FileAssetStatisticsResponse(
            @JsonProperty("totalCount") long totalCount,
            @JsonProperty("statusCounts") Map<String, Long> statusCounts,
            @JsonProperty("categoryCounts") Map<String, Long> categoryCounts) {
        this.totalCount = totalCount;
        this.statusCounts = statusCounts;
        this.categoryCounts = categoryCounts;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public Map<String, Long> getCategoryCounts() {
        return categoryCounts;
    }
}
