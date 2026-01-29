package or.my.project.itqgroup.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import or.my.project.itqgroup.util.DocumentStatus;

public record DocumentDtoResponse(
        Long id,
        String uniqueNumber,
        String author,
        String title,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<HistoryforDocumentResposeDto> history
) {
}
