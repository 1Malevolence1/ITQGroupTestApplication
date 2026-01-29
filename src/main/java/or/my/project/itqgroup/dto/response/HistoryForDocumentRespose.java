package or.my.project.itqgroup.dto.response;

import java.time.LocalDateTime;
import or.my.project.itqgroup.util.Action;

public record HistoryForDocumentRespose(
        Long id,
        String author,
        LocalDateTime timestamp,
        Action action,
        String comment
) {
}
