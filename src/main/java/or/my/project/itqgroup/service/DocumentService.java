package or.my.project.itqgroup.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.repository.DocumentRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public void create(CreateDocumentRequest request) {

        DocumentModel document = DocumentModel.builder()
                .title(request.titile())
                .author(request.author())
                .build();

        documentRepository.save(document);
        log.info("Документ успешно создан");
    }
}
