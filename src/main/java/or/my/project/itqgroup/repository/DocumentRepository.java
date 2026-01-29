package or.my.project.itqgroup.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {

  //  @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DocumentModel d WHERE d.id = :id")
    Optional<DocumentModel> findByIdWithLock(Long id);

  Page<DocumentModel> findByIdIn(List<Long> ids, Pageable pageable);

  Page<DocumentModel> findByAuthorAndStatusAndCreatedAtBetween(String author, DocumentStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);
    // Additional search methods as needed
}