-- pgvector 확장. 임베딩 검색용.
-- 이미지가 vector 확장을 포함해야 함 (docker: pgvector/pgvector:pg16).
-- POSTGRES_USER(farmily)가 컨테이너 초기화 시 슈퍼유저로 생성되므로
-- CREATE EXTENSION 권한 이슈는 없음. V1의 pgcrypto/citext와 동일 패턴.

CREATE EXTENSION IF NOT EXISTS vector;
