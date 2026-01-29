package or.my.project.itqgroup.dto.response;

import or.my.project.itqgroup.util.DocumentStatus;

public record ConcurrencyTestResponse(
        int successful,
        int conflicts,
        int errors,
        DocumentStatus finalStatus
) {}