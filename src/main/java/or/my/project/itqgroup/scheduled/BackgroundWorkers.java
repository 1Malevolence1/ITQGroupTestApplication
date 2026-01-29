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

    @Scheduled(fixedRateString = "${app.workers.submit-interval-ms}")
    public void submitWorker() {
        if (!workersEnabled) return;

        long startTotal = System.currentTimeMillis();
        int page = 0;

        log.info("SUBMIT-воркер: старт фоновой обработки документов");

        while (true) {
            long stepStart = System.currentTimeMillis();
            List<DocumentModel> docs = documentService.fetchBatch(DocumentStatus.DRAFT, PageRequest.of(page, batchSize));
            int totalDocs = docs.size();
            if (totalDocs == 0) break;

            List<Long> ids = docs.stream().map(DocumentModel::getId).toList();
            log.info("SUBMIT-воркер: загружена пачка документов ({} шт.) для отправки на подачу: {}", totalDocs, ids);

            List<BatchResponse> results = documentService.submitBatch(new BatchRequest(ids, "SUBMIT-WORKER", "Автоподача"));

            long success = results.stream().filter(r -> r.result() == BatchResponse.ProcessingResult.SUCCESS).count();
            long failed = results.size() - success;

            log.info("SUBMIT-воркер: обработана пачка {} → успешно: {}, неудачно: {}, время шага: {} мс",
                    ids.size(), success, failed, System.currentTimeMillis() - stepStart);

            if (docs.size() < batchSize) break;
            page++;
        }

        log.info("SUBMIT-воркер: завершена фонова обработка документов, общее время: {} мс", System.currentTimeMillis() - startTotal);
    }

    @Scheduled(fixedRateString = "${app.workers.approve-interval-ms:5000}")
    public void approveWorker() {
        if (!workersEnabled) return;

        long startTotal = System.currentTimeMillis();
        int page = 0;

        log.info("APPROVE-воркер: старт фоновой обработки документов");

        while (true) {
            long stepStart = System.currentTimeMillis();
            List<DocumentModel> docs = documentService.fetchBatch(DocumentStatus.SUBMITTED, PageRequest.of(page, batchSize));
            int totalDocs = docs.size();
            if (totalDocs == 0) break;

            List<Long> ids = docs.stream().map(DocumentModel::getId).toList();
            log.info("APPROVE-воркер: загружена пачка документов ({} шт.) для отправки на утверждение: {}", totalDocs, ids);

            List<BatchResponse> results = documentService.approveBatch(new BatchRequest(ids, "APPROVE-WORKER", "Автоутверждение"));

            long success = results.stream().filter(r -> r.result() == BatchResponse.ProcessingResult.SUCCESS).count();
            long failed = results.size() - success;

            log.info("APPROVE-воркер: обработана пачка {} → успешно: {}, неудачно: {}, время шага: {} мс",
                    ids.size(), success, failed, System.currentTimeMillis() - stepStart);

            if (docs.size() < batchSize) break;
            page++;
        }

        log.info("APPROVE-воркер: завершена фонова обработка документов, общее время: {} мс", System.currentTimeMillis() - startTotal);
    }
}


