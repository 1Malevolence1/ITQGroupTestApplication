package or.my.project.itqgroup.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @NotBlank(message = "Название не может быть пустым")
        String titile,

        @NotBlank(message = "Автор не может быть пустым")
        String author
) {
}
