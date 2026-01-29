package or.my.project.itqgroup.service;

import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.model.ApprovalRegistryModel;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.repository.ApprovalRegistryRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApprovalRegistryService {

    private final ApprovalRegistryRepository approvalRegistryRepository;

    public void save(DocumentModel document) {
        approvalRegistryRepository.save(ApprovalRegistryModel.builder()
                .document(document)
                .build());
    }
}
