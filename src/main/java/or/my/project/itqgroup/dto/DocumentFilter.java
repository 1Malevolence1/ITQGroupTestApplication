package or.my.project.itqgroup.dto;

import java.time.LocalDateTime;
import or.my.project.itqgroup.util.DocumentStatus;

public record DocumentFilter(
        String author,
        DocumentStatus status,
        LocalDateTime createdFrom,
        LocalDateTime createdTo
) {
}