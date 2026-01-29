package or.my.project.itqgroup;


import java.util.List;
import or.my.project.itqgroup.dto.request.BatchRequest;
import or.my.project.itqgroup.dto.response.BatchResponseDto;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.repository.DocumentRepository;
import or.my.project.itqgroup.service.ApprovalRegistryService;
import or.my.project.itqgroup.service.DocumentService;
import or.my.project.itqgroup.service.HistoryService;
import or.my.project.itqgroup.util.Action;
import or.my.project.itqgroup.util.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DocumentServiceMockitoTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private HistoryService historyService;

    @Mock
    private ApprovalRegistryService approvalRegistryService;

    @InjectMocks
    private DocumentService documentService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitBatch_singleDocument_success() {

        DocumentModel doc = new DocumentModel();
        doc.setId(1L);
        doc.setStatus(DocumentStatus.DRAFT);

        when(documentRepository.findAllByIdWithLock(List.of(1L)))
                .thenReturn(List.of(doc));

        BatchRequest request = new BatchRequest(List.of(1L), "user", "комментарий");


        List<BatchResponseDto> results = documentService.submitBatch(request);


        assertEquals(1, results.size());
        assertEquals(DocumentStatus.SUBMITTED, doc.getStatus());
        assertEquals(BatchResponseDto.ProcessingResult.SUCCESS, results.get(0).result());

        verify(historyService).save(doc, "user", Action.SUBMIT, "комментарий");
        verifyNoMoreInteractions(approvalRegistryService);
    }

    @Test
    void approveBatch_singleDocument_success() {
        DocumentModel doc = new DocumentModel();
        doc.setId(1L);
        doc.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findAllByIdWithLock(List.of(1L)))
                .thenReturn(List.of(doc));

        BatchRequest request = new BatchRequest(List.of(1L), "user", "комментарий");

        List<BatchResponseDto> results = documentService.approveBatch(request);

        assertEquals(1, results.size());
        assertEquals(DocumentStatus.APPROVED, doc.getStatus());
        assertEquals(BatchResponseDto.ProcessingResult.SUCCESS, results.get(0).result());

        verify(approvalRegistryService).save(doc);
        verify(historyService).save(doc, "user", Action.APPROVE, "комментарий");
    }

    @Test
    void approveBatch_partialFailure_registryError() {
        DocumentModel doc1 = new DocumentModel();
        doc1.setId(1L);
        doc1.setStatus(DocumentStatus.SUBMITTED);

        DocumentModel doc2 = new DocumentModel();
        doc2.setId(2L);
        doc2.setStatus(DocumentStatus.SUBMITTED);

        when(documentRepository.findAllByIdWithLock(List.of(1L, 2L)))
                .thenReturn(List.of(doc1, doc2));

        doThrow(new RuntimeException("Registry error"))
                .when(approvalRegistryService).save(doc2);

        BatchRequest request = new BatchRequest(List.of(1L, 2L), "user", "комментарий");

        List<BatchResponseDto> results = documentService.approveBatch(request);

        assertEquals(2, results.size());


        assertEquals(DocumentStatus.APPROVED, doc1.getStatus());
        assertEquals(BatchResponseDto.ProcessingResult.SUCCESS, results.get(0).result());


        assertEquals(BatchResponseDto.ProcessingResult.REGISTRY_ERROR, results.get(1).result());

        verify(approvalRegistryService).save(doc1);
        verify(approvalRegistryService).save(doc2);
    }

    @Test
    void submitBatch_batchProcessing_multipleDocuments() {
        DocumentModel doc1 = new DocumentModel();
        doc1.setId(1L);
        doc1.setStatus(DocumentStatus.DRAFT);

        DocumentModel doc2 = new DocumentModel();
        doc2.setId(2L);
        doc2.setStatus(DocumentStatus.SUBMITTED);

        DocumentModel doc3 = new DocumentModel();
        doc3.setId(3L);
        doc3.setStatus(DocumentStatus.DRAFT);

        when(documentRepository.findAllByIdWithLock(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(doc1, doc2, doc3));

        BatchRequest request = new BatchRequest(List.of(1L, 2L, 3L), "user", "комментарий");

        List<BatchResponseDto> results = documentService.submitBatch(request);

        assertEquals(3, results.size());
        assertEquals(BatchResponseDto.ProcessingResult.SUCCESS, results.get(0).result());
        assertEquals(BatchResponseDto.ProcessingResult.ALREADY, results.get(1).result());
        assertEquals(BatchResponseDto.ProcessingResult.SUCCESS, results.get(2).result());
    }
}
