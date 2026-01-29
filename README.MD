# ITQGroup Document Service

Сервис управления документами с поддержкой пакетной обработки, фоновых воркеров
и тестирования конкурентного утверждения документов.

## Требования

- Java 25+
- Docker + Docker Compose
- PostgreSQL (через Docker)
- Maven

---

## Запуск проекта

### 1. Перейти в корневую директорию проекта

```bash
cd ITQGroup
`````
### 2. Запустить инфраструктуру (PostgreSQL)

```bash
docker-compose up -d
```
Убедись, что контейнер с PostgreSQL запущен и доступен на localhost:5432.

### 3. Запустить Spring Boot приложение
```bash
./mvnw spring-boot:run
```
Приложение будет запущенно на порту 8080

## Конфигурация (application.properties)

Пакетная обработка документов
```
app.batch-size=100
```
Размер пачки документов, используемый:

SUBMIT-worker
APPROVE-worker

batch API (/submit, /approve)

Фоновые воркеры

```
app.workers.enabled=false
app.workers.submit-interval-ms=30000
app.workers.approve-interval-ms=30000
```

* enabled=false — воркеры отключены (ручное управление через API)
* submit-interval-ms — период отправки DRAFT → SUBMITTED
* approve-interval-ms — период отправки SUBMITTED → APPROVED

### Генерация документов (утилита)
```
app.documents.create-enabled=false
app.documents.count=100
app.documents.default-author=SYSTEM
app.documents.default-title-prefix=Document
```

Если create-enabled=true, при старте приложения:

будет создано count документов через публичное API сервиса используется CommandLineRunner

Прогресс генерации можно отслеживать в логах приложения.

## REST API
### Concurrency Test

* POST /concurrency-test

Запускает конкурентный тест для документа.

Параметры запроса:

* documentId — id документа,
* threads — количество потоков,
* attempts — количество попыток в каждом потоке.

Пример:
```
curl -X POST "http://localhost:8080/concurrency-test?documentId=1&threads=10&attempts=100"
```
Пример ответа:
```
{
"totalAttempts": 1000,
"successful": 980,
"failed": 20,
"durationMs": 1540
}
```

где:

* successful — успешные операции
* conflicts — конфликты версий / блокировок
* errors — ошибки выполнения
* finalStatus — финальный статус документа

## Documents API

Базовый путь: /api/v1/documents

Создание документа

* POST /api/v1/documents

Создаёт новый документ.
```
curl -X POST http://localhost:8080/api/v1/documents \
-H "Content-Type: application/json" \
-d '{
"title": "Test document",
"content": "Some content",
"author": "john.doe"
}'

```
Ответ: 204 No Content

* Получение документа

GET /api/v1/documents/{id}
```
curl http://localhost:8080/api/v1/documents/1
``` 

Пример ответа: 
```
{
"id": 1,
"uniqueNumber": "DOC-2026-0001",
"author": "john.doe",
"title": "Test document",
"status": "DRAFT",
"createdAt": "2026-01-29T10:15:30",
"updatedAt": "2026-01-29T10:20:10",
"history": [
{
"id": 10,
"author": "john.doe",
"timestamp": "2026-01-29T10:15:30",
"action": "CREATED",
"comment": "Документ создан"
}
]
}
```
Получение списка документов

* GET /api/v1/documents

Поддерживает фильтрацию по ids, author, status, диапазону дат создания, а также пагинацию и сортировку.
```
curl "http://localhost:8080/api/v1/documents?ids=1,2,3&author=john.doe&status=DRAFT&page=0&size=10"
``` 

Пример ответа:
```
{
"items": [
{
"id": 1,
"uniqueNumber": "DOC-2026-0001",
"author": "john.doe",
"title": "Test document",
"status": "DRAFT",
"createdAt": "2026-01-29T10:15:30",
"updatedAt": "2026-01-29T10:20:10",
"history": []
}
],
"page": 0,
"size": 10,
"total": 1
}
```
Отправка документов на согласование

* POST /api/v1/documents/submit

Batch-операция для перевода документов в статус SUBMITTED.
````
curl -X POST http://localhost:8080/api/v1/documents/submit \
-H "Content-Type: application/json" \
-d '{
"documentIds": [1,2,3],
"user": "john.doe"
}'
````
Пример ответа: 

```
[
  {
    "id": 1,
    "result": "успешно"
  },
  {
    "id": 2,
    "result": "конфликт"
  }
]
```

Подтверждение документов

* POST /api/v1/documents/approve

Batch-операция для подтверждения документов.
````
curl -X POST http://localhost:8080/api/v1/documents/approve \
-H "Content-Type: application/json" \
-d '{
"documentIds": [1,2,3],
"user": "approver.user"
}'
````

Пример ответа:

````
[
  {
    "id": 3,
    "result": "успешно"
  },
  {
    "id": 4,
    "result": "Уже имеет нужный статус"
  }
]
````