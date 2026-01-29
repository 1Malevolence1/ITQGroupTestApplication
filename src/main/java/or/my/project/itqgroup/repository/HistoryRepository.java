package or.my.project.itqgroup.repository;

import or.my.project.itqgroup.model.HistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<HistoryModel, Long> {
}
