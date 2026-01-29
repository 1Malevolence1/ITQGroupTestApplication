package or.my.project.itqgroup.dto.request;

public record ConcurrencyTestRequest(
        Long documentId,
        int threads,
        int attempts
) {}