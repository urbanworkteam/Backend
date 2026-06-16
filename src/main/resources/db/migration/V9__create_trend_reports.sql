-- Farmily AgentCore — 트렌드 리포트 벡터 테이블
-- search_trend tool이 최근 7일 트렌드를 벡터 유사도로 검색하는 데 사용

CREATE TABLE IF NOT EXISTS trend_reports (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    report_date DATE        NOT NULL,
    source      TEXT        NOT NULL,
    content     TEXT        NOT NULL,
    embedding   vector(1024),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_trend_date      ON trend_reports (report_date DESC);
CREATE INDEX IF NOT EXISTS idx_trend_source    ON trend_reports (source);
CREATE INDEX IF NOT EXISTS idx_trend_embedding ON trend_reports USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 10);
