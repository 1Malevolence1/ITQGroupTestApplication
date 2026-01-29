package or.my.project.itqgroup.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.request.BatchRequest;
import or.my.project.itqgroup.dto.response.BatchResponseDto;
import or.my.project.itqgroup.dto.response.ConcurrencyTestResponse;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.repository.DocumentRepository;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ConcurrencyTestService {

    private final DocumentService documentService;

    public ConcurrencyTestResponse runTest(Long documentId, int threads, int attempts) {
        if (documentId == null || threads <= 0 || attempts <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "documentId должен быть указан, threads и attempts должны быть больше 0"
            );
        }

        DocumentModel doc = documentService.getById(documentId);

        if (doc.getStatus() == DocumentStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Документ уже имеет статус APPROVED, повторное утверждение невозможно"
            );
        }

        if (doc.getStatus() == DocumentStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Документ имеет статус DRAFT и не может быть утверждён"
            );
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < attempts; i++) {
            executor.submit(() -> {
                try {
                    BatchRequest batchReq = new BatchRequest(
                            List.of(documentId),
                            "concurrency-test-" + Thread.currentThread().getName(),
                            "Concurrent test attempt"
                    );

                    BatchResponseDto res = documentService.approveBatch(batchReq).get(0);

                    switch (res.result()) {
                        case SUCCESS -> successCount.incrementAndGet();
                        case CONFLICT, ALREADY -> conflictCount.incrementAndGet();
                        case ERROR -> errorCount.incrementAndGet();
                    }
                } catch (DataIntegrityViolationException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        DocumentModel finalDoc = documentService.getById(documentId);

        return new ConcurrencyTestResponse(
                successCount.get(),
                conflictCount.get(),
                errorCount.get(),
                finalDoc.getStatus()
        );
    }
}

