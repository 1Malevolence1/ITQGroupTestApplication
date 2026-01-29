package or.my.project.itqgroup.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.model.HistoryModel;
import or.my.project.itqgroup.repository.HistoryRepository;
import or.my.project.itqgroup.util.Action;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryService {

    private final HistoryRepository historyRepository;


    public void save(DocumentModel document, String auhtor, Action action, String comment) {
        historyRepository.save(
                HistoryModel.builder()
                        .document(document)
                        .author(auhtor)
                        .action(action)
                        .comment(comment)
                        .build()
        );
    }
}
