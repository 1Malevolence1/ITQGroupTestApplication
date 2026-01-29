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
import or.my.project.itqgroup.dto.request.DocumentIdsReuqest;
import or.my.project.itqgroup.dto.response.BatchResponseDto;
import or.my.project.itqgroup.dto.response.DocumentResponseDto;
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
    private final DocumentMapper documentMapper;

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
    public DocumentResponseDto get(Long id) {
        DocumentModel documentModel = documentRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "не найдено")
        );
        return documentMapper.toDto(documentModel);
    }

    @Transactional(readOnly = true)
    public ApiListResponse<DocumentResponseDto> getAll(DocumentFilter filter, List<Long> ids, int page, int size, CustomSortDescription sortDirection) {


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
    public List<BatchResponseDto> submitBatch(BatchRequest request) {
        List<BatchResponseDto> responses = new ArrayList<>(request.ids().size());

        List<DocumentModel> docs = documentRepository.findAllByIdWithLock(request.ids());

        Map<Long, DocumentModel> docMap = docs.stream()
                .collect(Collectors.toMap(DocumentModel::getId, d -> d));

        for (Long id : request.ids()) {
            DocumentModel doc = docMap.get(id);

            if (doc == null) {
                responses.add(BatchResponseDto.notFound(id));
                continue;
            }

            if (doc.getStatus() != DocumentStatus.DRAFT) {
                responses.add(BatchResponseDto.conflict(id));
                continue;
            }

            doc.setStatus(DocumentStatus.SUBMITTED);
            historyService.save(doc, request.author(), Action.SUBMIT, request.comment());

            responses.add(BatchResponseDto.success(id));
        }

        return responses;
    }
}

