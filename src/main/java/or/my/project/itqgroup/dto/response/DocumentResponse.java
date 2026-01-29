package or.my.project.itqgroup.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import or.my.project.itqgroup.util.DocumentStatus;

public record DocumentResponse(
        Long id,
        String uniqueNumber,
        String author,
        String title,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<HistoryForDocumentRespose> history
) {
}
