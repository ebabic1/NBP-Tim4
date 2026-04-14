package ba.unsa.etf.nbp.travel.util;

import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PaginationUtil {

    public static <T> PageResponse<T> buildPageResponse(List<T> content, int page, int size, long totalElements) {
        var safeSize = Math.clamp(size, 1, 100);
        var totalPages = (int) Math.ceil((double) totalElements / safeSize);
        return new PageResponse<>(content, page, safeSize, totalElements, totalPages);
    }
}
