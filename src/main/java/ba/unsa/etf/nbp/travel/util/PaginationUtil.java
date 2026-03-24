package ba.unsa.etf.nbp.travel.util;

import ba.unsa.etf.nbp.travel.dto.response.PageResponse;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PaginationUtil {

    public static <T> PageResponse<T> buildPageResponse(List<T> content, int page, int size, long totalElements) {
        var totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
