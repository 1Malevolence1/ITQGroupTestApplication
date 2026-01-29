package or.my.project.itqgroup.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentInitializerRunner implements CommandLineRunner {

    private final DocumentService documentService;

    @Value("${app.documents.create-enabled:false}")
    private boolean createEnabled;

    @Value("${app.documents.count:10}")
    private int documentCount;

    @Value("${app.documents.default-author:SYSTEM}")
    private String defaultAuthor;

    @Value("${app.documents.default-title-prefix:Документ}")
    private String defaultTitlePrefix;

    @Override
    public void run(String... args) throws Exception {
        if (!createEnabled) {
            log.info("Генерация документов отключена (create-enabled=false)");
            return;
        }

        log.info("Начинаем генерацию {} документов", documentCount);

        long start = System.currentTimeMillis();

        for (int i = 1; i <= documentCount; i++) {
            String title = defaultTitlePrefix + " " + i;
            CreateDocumentRequest request = new CreateDocumentRequest(title, defaultAuthor);
            documentService.create(request);
        }

        log.info("Генерация документов завершена. Создано {} документов, время: {} мс",
                documentCount, System.currentTimeMillis() - start);
    }
}
