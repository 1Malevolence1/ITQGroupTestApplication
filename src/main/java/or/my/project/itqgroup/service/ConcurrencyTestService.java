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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConcurrencyTestService {

    private final DocumentService documentService;
    private final DocumentRepository documentRepository;

    public ConcurrencyTestResponse runTest(Long documentId, int threads, int attempts) {
        if (documentId == null || threads <= 0 || attempts <= 0) {
            throw new IllegalArgumentException("Неверные параметры: documentId должен быть указан, threads и attempts > 0");
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

                    List<BatchResponseDto> results = documentService.approveBatch(batchReq);

                    BatchResponseDto res = results.get(0);

                    // Логика обработки уникальности и статуса
                    switch (res.result()) {
                        case SUCCESS -> successCount.incrementAndGet();
                        case CONFLICT -> conflictCount.incrementAndGet();
                        case ERROR -> errorCount.incrementAndGet();
                    }

                } catch (DataIntegrityViolationException e) {
                    // Если попытка вставки в реестр упала из-за уникального ключа
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

        // Получаем финальный статус документа
        DocumentModel finalDoc = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document disappeared"));

        return new ConcurrencyTestResponse(
                successCount.get(),
                conflictCount.get(),
                errorCount.get(),
                finalDoc.getStatus()
        );
    }
}

