package or.my.project.itqgroup.repository.specification;

import java.time.LocalDateTime;
import java.util.List;
import lombok.experimental.UtilityClass;
import or.my.project.itqgroup.model.DocumentModel;
import or.my.project.itqgroup.util.DocumentStatus;
import org.springframework.data.jpa.domain.Specification;


public final class DocumentSpecification {

    private DocumentSpecification() {
    }

    public static Specification<DocumentModel> authorEquals(String author) {
        return (root, query, cb) ->
                author == null ? cb.conjunction()
                        : cb.equal(root.get("author"), author);
    }

    public static Specification<DocumentModel> statusEquals(DocumentStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction()
                        : cb.equal(root.get("status"), status);
    }

    public static Specification<DocumentModel> createdFrom(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? cb.conjunction()
                        : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<DocumentModel> createdTo(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? cb.conjunction()
                        : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<DocumentModel> idIn(List<Long> ids) {
        return (root, query, cb) ->
                ids == null || ids.isEmpty()
                        ? cb.conjunction()
                        : root.get("id").in(ids);
    }
}

