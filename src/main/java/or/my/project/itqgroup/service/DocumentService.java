package or.my.project.itqgroup.service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.my.project.itqgroup.dto.DocumentFilter;
import or.my.project.itqgroup.dto.request.BatchRequest;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.dto.response.BatchResponse;
import or.my.project.itqgroup.dto.response.DocumentResponse;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.repository.DocumentRepository;
import or.my.project.itqgroup.repository.specification.DocumentSpecification;
import or.my.project.itqgroup.service.mapper.DocumentMapper;
import or.my.project.itqgroup.util.Action;
import or.my.project.itqgroup.util.ApiListResponse;
import or.my.project.itqgroup.util.CustomSortDescription;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final HistoryService historyService;
    private final ApprovalRegistryService approvalRegistryService;
    private final DocumentMapper documentMapper;



    public List<DocumentModel> fetchBatch(DocumentStatus status, PageRequest pageRequest) {
        return documentRepository.findByStatus(status, pageRequest);
    }


    @Transactional
    public void create(CreateDocumentRequest request) {

        DocumentModel document = DocumentModel.builder()
                .title(request.titile())
                .author(request.author())
                .build();

        documentRepository.save(document);
        log.info("Документ успешно создан");
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(Long id) {
        return documentMapper.toDto(getById(id));
    }

    public DocumentModel getById(Long id){
       return documentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "не найдено")
        );
    }

    @Transactional(readOnly = true)
    public ApiListResponse<DocumentResponse> getAll(DocumentFilter filter, List<Long> ids, int page, int size, CustomSortDescription sortDirection) {


        Sort sort = sortDirection == CustomSortDescription.ASC ? Sort.by("id").ascending() : Sort.by("id").descending();

        Pageable pageable = PageRequest.of(page, size, sort);


        Specification<DocumentModel> spec = Specification
                .where(DocumentSpecification.idIn(ids)
                .and(DocumentSpecification.authorEquals(filter.author()))
                .and(DocumentSpecification.statusEquals(filter.status()))
                .and(DocumentSpecification.createdFrom(filter.createdFrom()))
                .and(DocumentSpecification.createdTo(filter.createdTo())));


        Page<DocumentModel> documentPage =
                documentRepository.findAll(spec, pageable);


        return new ApiListResponse<>(documentMapper.toDtoList(documentPage.getContent()), documentPage.getTotalElements());
    }

    @Transactional
    public List<BatchResponse> submitBatch(BatchRequest request) {

        List<BatchResponse> responses = new ArrayList<>(request.ids().size());
        List<Long> successIds = new ArrayList<>();

        List<DocumentModel> docs =
                documentRepository.findAllByIdWithLock(request.ids());

        Map<Long, DocumentModel> docMap = docs.stream()
                .collect(Collectors.toMap(DocumentModel::getId, d -> d));

        for (Long id : request.ids()) {
            DocumentModel doc = docMap.get(id);

            if (doc == null) {
                responses.add(BatchResponse.notFound(id));
                continue;
            }

            if (doc.getStatus() == DocumentStatus.SUBMITTED) {
                responses.add(BatchResponse.already(id));
                continue;
            }

            if (doc.getStatus() != DocumentStatus.DRAFT) {
                responses.add(BatchResponse.conflict(id));
                continue;
            }

            try {

                historyService.save(doc, request.author(), Action.SUBMIT, request.comment());
                successIds.add(id);
                responses.add(BatchResponse.success(id));

            } catch (RuntimeException e) {
                responses.add(BatchResponse.registryError(id));
            }
        }


        if (!successIds.isEmpty()) {
            documentRepository.updateStatusBatch(
                    successIds,
                    DocumentStatus.SUBMITTED
            );
        }

        return responses;
    }


    @Transactional
    public List<BatchResponse> approveBatch(BatchRequest request) {

        List<BatchResponse> responses = new ArrayList<>(request.ids().size());
        List<Long> approvedIds = new ArrayList<>();


        List<DocumentModel> docs = documentRepository.findAllByIdWithLock(request.ids());
        Map<Long, DocumentModel> docMap = docs.stream()
                .collect(Collectors.toMap(DocumentModel::getId, d -> d));

        for (Long id : request.ids()) {
            DocumentModel doc = docMap.get(id);

            if (doc == null) {
                responses.add(BatchResponse.notFound(id));
                continue;
            }

            if (doc.getStatus() == DocumentStatus.APPROVED) {
                responses.add(BatchResponse.already(id));
                continue;
            }

            if (doc.getStatus() != DocumentStatus.SUBMITTED) {
                responses.add(BatchResponse.conflict(id));
                continue;
            }

            try {

                approvalRegistryService.save(doc);
                historyService.save(doc, request.author(), Action.APPROVE, request.comment());

                approvedIds.add(id);
                responses.add(BatchResponse.success(id));

            } catch (RuntimeException e) {
                responses.add(BatchResponse.registryError(id));
                   /* Так как нет возможность задать уточняющие вопросы,
                        то напишу в комментариях. Я понимаю, что если при записи в регистр произошла ошибка,
                        то запись в итсории не появится, а вот что делать, если при сохранении истории
                         появится ошибка- в тз не указано.
                         Тут, конечно, можно было добавить логику, которая бы чистила историю и утверждённые документы
                         */
            }
        }


        if (!approvedIds.isEmpty()) {
            documentRepository.updateStatusBatch(approvedIds, DocumentStatus.APPROVED);
        }

        return responses;
    }
}

