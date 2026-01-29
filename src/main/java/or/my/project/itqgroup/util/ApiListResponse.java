package or.my.project.itqgroup.util;

import java.util.List;

public record ApiListResponse<T>(
        List<T> results,
        Long total
) {
}
