-- noinspection SqlResolveForFile @ object-type/"vector"

create table public.documents (
                                  id bigserial not null,
                                  content text null,
                                  embedding extensions.vector null,
                                  constraint documents_pkey primary key (id)
) TABLESPACE pg_default;



-- Create a function to search for documents
create or replace function match_documents (
    query_embedding vector(1536),
    match_threshold float,
    match_count int
)
    returns table (
                      id bigint,
                      content text,
                      similarity float
                  )
    language sql stable
as $$
select
    documents.id,
    documents.content,
    1 - (documents.embedding <=> query_embedding) as similarity
from documents
where 1 - (documents.embedding <=> query_embedding) > match_threshold
order by similarity desc
limit match_count;
$$;