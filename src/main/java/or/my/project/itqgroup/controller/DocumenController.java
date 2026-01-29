package or.my.project.itqgroup.controller;


import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.DocumentFilter;
import or.my.project.itqgroup.dto.request.BatchRequest;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.dto.response.BatchResponse;
import or.my.project.itqgroup.dto.response.DocumentResponse;
import or.my.project.itqgroup.service.DocumentService;
import or.my.project.itqgroup.util.ApiListResponse;
import or.my.project.itqgroup.util.CustomSortDescription;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.format.annotation.DateTimeFormat;
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
    public ResponseEntity<DocumentResponse> get(@PathVariable Long id) {

        return ResponseEntity.ok(
                documentService.get(id)
        );
    }


    @GetMapping()
    public ApiListResponse<DocumentResponse> getAll(
            @RequestParam List<Long> ids,

            @RequestParam(required = false) String author,
            @RequestParam(required = false) DocumentStatus status,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdFrom,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime createdTo,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "ASC") CustomSortDescription sort
    ) {

       DocumentFilter filter = new DocumentFilter(
                author,
                status,
                createdFrom,
                createdTo
        );

        return documentService.getAll(
                filter,
                ids,
                page,
                size,
                sort
        );
    }


    @PostMapping("/submit")
    public ResponseEntity<List<BatchResponse>> submit(@RequestBody BatchRequest request) {
        return ResponseEntity.ok(documentService.submitBatch(request));
    }

    @PostMapping("/approve")
    public ResponseEntity<List<BatchResponse>> approve(@RequestBody BatchRequest request) {
        return ResponseEntity.ok(documentService.approveBatch(request));
    }
}
