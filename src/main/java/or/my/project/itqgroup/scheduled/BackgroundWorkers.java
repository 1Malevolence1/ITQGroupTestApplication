package or.my.project.itqgroup.scheduled;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.my.project.itqgroup.dto.request.BatchRequest;
import or.my.project.itqgroup.dto.response.BatchResponse;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.service.DocumentService;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackgroundWorkers {

    private final DocumentService documentService;

    @Value("${app.batch-size}")
    private int batchSize;

    @Value("${app.workers.enabled}")
    private boolean workersEnabled;

    @Scheduled(fixedDelayString = "${app.workers.submit-interval-ms:5000}")
    public void submitWorker() {
        if (!workersEnabled) return;

        long startTotal = System.currentTimeMillis();

        long totalToProcess = documentService.countByStatus(DocumentStatus.DRAFT);
        long processed = 0;

        log.info("SUBMIT-воркер: старт фоновой обработки документов. Всего DRAFT документов: {}", totalToProcess);

        Pageable pageable = PageRequest.of(0, batchSize);
        Slice<DocumentModel> slice;

        do {
            long stepStart = System.currentTimeMillis();

            slice = documentService.fetchSlice(DocumentStatus.DRAFT, pageable);
            List<DocumentModel> docs = slice.getContent();

            if (docs.isEmpty()) break;

            List<Long> ids = docs.stream().map(DocumentModel::getId).toList();
            log.info("SUBMIT-воркер: загружена пачка документов ({} шт.) для отправки на подачу: {}", ids.size(), ids);

            List<BatchResponse> results = documentService.submitBatch(new BatchRequest(ids, "SUBMIT-WORKER", "Автоподача"));

            long success = results.stream().filter(r -> r.result() == BatchResponse.ProcessingResult.SUCCESS).count();
            long failed = results.size() - success;

            processed += docs.size();

            log.info(
                    "SUBMIT-воркер: обработана пачка {} → успешно: {}, неудачно: {}, время шага: {} мс, прогресс: {}/{}",
                    ids.size(), success, failed, System.currentTimeMillis() - stepStart, processed, totalToProcess
            );

            pageable = slice.nextPageable();
        } while (slice.hasNext());

        log.info("SUBMIT-воркер: завершена фонова обработка документов, общее время: {} мс, всего обработано: {}",
                System.currentTimeMillis() - startTotal, processed);
    }


    @Scheduled(fixedDelayString = "${app.workers.approve-interval-ms:5000}")
    public void approveWorker() {
        if (!workersEnabled) return;

        long startTotal = System.currentTimeMillis();

        long totalToProcess = documentService.countByStatus(DocumentStatus.SUBMITTED);
        long processed = 0;

        log.info("APPROVE-воркер: старт фоновой обработки документов. Всего SUBMITTED документов: {}", totalToProcess);

        Pageable pageable = PageRequest.of(0, batchSize);
        Slice<DocumentModel> slice;

        do {
            long stepStart = System.currentTimeMillis();

            slice = documentService.fetchSlice(DocumentStatus.SUBMITTED, pageable);
            List<DocumentModel> docs = slice.getContent();

            if (docs.isEmpty()) break;

            List<Long> ids = docs.stream().map(DocumentModel::getId).toList();
            log.info("APPROVE-воркер: загружена пачка документов ({} шт.) для отправки на утверждение: {}", ids.size(), ids);

            List<BatchResponse> results = documentService.approveBatch(new BatchRequest(ids, "APPROVE-WORKER", "Автоутверждение"));

            long success = results.stream().filter(r -> r.result() == BatchResponse.ProcessingResult.SUCCESS).count();
            long failed = results.size() - success;

            processed += docs.size();

            log.info(
                    "APPROVE-воркер: обработана пачка {} → успешно: {}, неудачно: {}, время шага: {} мс, прогресс: {}/{}",
                    ids.size(), success, failed, System.currentTimeMillis() - stepStart, processed, totalToProcess
            );

            pageable = slice.nextPageable();
        } while (slice.hasNext());

        log.info("APPROVE-воркер: завершена фонова обработка документов, общее время: {} мс, всего обработано: {}",
                System.currentTimeMillis() - startTotal, processed);
    }
}


