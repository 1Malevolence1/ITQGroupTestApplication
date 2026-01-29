package or.my.project.itqgroup.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.dto.request.DocumentIdsReuqest;
import or.my.project.itqgroup.dto.response.DocumentDtoResponse;
import or.my.project.itqgroup.service.DocumentService;
import or.my.project.itqgroup.util.ApiListResponse;
import or.my.project.itqgroup.util.CustomSortDescription;
import or.my.project.itqgroup.util.DocumentStatus;
import org.hibernate.query.SortDirection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumenController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateDocumentRequest request) {
        documentService.create(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDtoResponse> get(@PathVariable Long id) {

        return ResponseEntity.ok(
                documentService.get(id)
        );
    }
    @GetMapping
    public ResponseEntity<ApiListResponse<DocumentDtoResponse>> getAll(
            @RequestBody DocumentIdsReuqest request,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "DESC") CustomSortDescription sort) {
        return ResponseEntity.ok(
                documentService.getAll(request, page, size ,sort)
        );
    }
}
