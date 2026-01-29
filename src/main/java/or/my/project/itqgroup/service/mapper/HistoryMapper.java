package or.my.project.itqgroup.service.mapper;


import java.util.Collections;
import java.util.List;
import or.my.project.itqgroup.dto.response.HistoryforDocumentResposeDto;
import or.my.project.itqgroup.model.HistoryModel;
import org.springframework.stereotype.Component;

@Component
public class HistoryMapper {

    public HistoryforDocumentResposeDto toDtoForDucument(HistoryModel history) {
        if (history == null) {
            return null;
        }

        return new HistoryforDocumentResposeDto(
                history.getId(),
                history.getAuthor(),
                history.getTimestamp(),
                history.getAction(),
                history.getComment()
        );
    }

    public List<HistoryforDocumentResposeDto> toDtoListDtoForDucument(List<HistoryModel> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }

        return histories.stream()
                .map(this::toDtoForDucument)
                .toList();
    }
}
