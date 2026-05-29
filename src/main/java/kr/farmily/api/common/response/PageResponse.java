package kr.farmily.api.common.response;

import java.util.List;

public record PageResponse<T>(List<T> data, String nextCursor, boolean hasMore) {

    public static <T> PageResponse<T> of(List<T> data, String nextCursor, boolean hasMore) {
        return new PageResponse<>(data, nextCursor, hasMore);
    }
}
