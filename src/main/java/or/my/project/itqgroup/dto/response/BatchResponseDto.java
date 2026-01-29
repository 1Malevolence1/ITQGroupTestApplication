package or.my.project.itqgroup.dto.response;



public record BatchResponseDto(
        Long id,
        ProcessingResult result
) {

    public enum ProcessingResult {
        SUCCESS("успешно"),
        NOT_FOUND("не найдено"),
        CONFLICT("конфликт"),
        REGISTRY_ERROR("ошибка регистрации в реестре"),
        ERROR("ошибка");

        private final String code;

        ProcessingResult(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        @Override
        public String toString() {
            return code;
        }
    }


    public static BatchResponseDto success(Long id) {
        return new BatchResponseDto(id, ProcessingResult.SUCCESS);
    }

    public static BatchResponseDto notFound(Long id) {
        return new BatchResponseDto(id, ProcessingResult.NOT_FOUND);
    }

    public static BatchResponseDto conflict(Long id) {
        return new BatchResponseDto(id, ProcessingResult.CONFLICT);
    }

    public static BatchResponseDto registryError(Long id) {
        return new BatchResponseDto(id, ProcessingResult.REGISTRY_ERROR);
    }

    public static BatchResponseDto error(Long id, String details) {
        return new BatchResponseDto(id, ProcessingResult.ERROR);
    }
}