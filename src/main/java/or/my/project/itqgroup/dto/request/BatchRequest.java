package or.my.project.itqgroup.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchRequest(

        @NotEmpty(message = "Список идентификаторов не должен быть пустым")
        @Size(
                min = 1,
                max = 1000,
                message = "Количество идентификаторов должно быть от 1 до 1000"
        )
        List<Long> ids,

        @NotBlank(message = "Автор не должен быть пустым")
        String author,

        String comment
) {
}