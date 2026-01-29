package or.my.project.itqgroup.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.request.CreateDocumentRequest;
import or.my.project.itqgroup.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
