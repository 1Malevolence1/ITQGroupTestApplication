package or.my.project.itqgroup.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentModel, Long>, JpaSpecificationExecutor<DocumentModel> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
       SELECT DISTINCT d
       FROM DocumentModel d
       LEFT JOIN FETCH d.histories h
       WHERE d.id IN :ids
       """)
  List<DocumentModel> findAllByIdWithLock(@Param("ids") Collection<Long> ids);

}