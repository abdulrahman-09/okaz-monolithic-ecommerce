package com.am9.okazx.dto.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int pageNo,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
