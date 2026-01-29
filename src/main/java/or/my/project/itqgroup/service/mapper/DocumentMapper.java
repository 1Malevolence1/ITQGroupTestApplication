package or.my.project.itqgroup.service.mapper;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import or.my.project.itqgroup.dto.response.DocumentResponseDto;
import or.my.project.itqgroup.model.DocumentModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentMapper {

    private final HistoryMapper historyMapper;

    public DocumentResponseDto toDto(DocumentModel model) {
        return new DocumentResponseDto(
                model.getId(),
                model.getUniqueNumber(),
                model.getAuthor(),
                model.getTitle(),
                model.getStatus(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                historyMapper.toDtoListDtoForDucument(model.getHistories())
        );
    }

    public List<DocumentResponseDto> toDtoList(List<DocumentModel> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }

        return models.stream()
                .map(this::toDto)
                .toList();
    }
}
