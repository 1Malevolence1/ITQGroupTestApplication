package or.my.project.itqgroup.dto.response;



public record BatchResponse(
        Long id,
        ProcessingResult result
) {

    public enum ProcessingResult {
        SUCCESS("успешно"),
        NOT_FOUND("не найдено"),
        CONFLICT("конфликт"),
        ALREADY("Уже имеет нужный статус"),
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


    public static BatchResponse success(Long id) {
        return new BatchResponse(id, ProcessingResult.SUCCESS);
    }

    public static BatchResponse notFound(Long id) {
        return new BatchResponse(id, ProcessingResult.NOT_FOUND);
    }

    public static BatchResponse conflict(Long id) {
        return new BatchResponse(id, ProcessingResult.CONFLICT);
    }

    public static BatchResponse registryError(Long id) {
        return new BatchResponse(id, ProcessingResult.REGISTRY_ERROR);
    }

    public static BatchResponse already(Long id) {
        return new BatchResponse(id, ProcessingResult.ALREADY);
    }

    public static BatchResponse error(Long id, String details) {
        return new BatchResponse(id, ProcessingResult.ERROR);
    }

}