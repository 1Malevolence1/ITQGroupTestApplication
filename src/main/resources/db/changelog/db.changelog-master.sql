
CREATE TABLE document_model (
    id BIGSERIAL PRIMARY KEY,
    unique_number VARCHAR(255) NOT NULL UNIQUE,
    author VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);


CREATE TABLE history_model (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    author VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    action VARCHAR(50) NOT NULL,
    comment TEXT,
    CONSTRAINT fk_history_document FOREIGN KEY (document_id)
        REFERENCES document_model (id)
);


CREATE TABLE approval_registry_model (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL UNIQUE,
    registered_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_approval_document FOREIGN KEY (document_id)
        REFERENCES document_model (id)
);
