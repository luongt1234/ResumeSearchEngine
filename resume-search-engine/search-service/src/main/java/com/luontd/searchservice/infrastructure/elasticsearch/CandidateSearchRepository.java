package com.luontd.searchservice.infrastructure.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateSearchRepository extends ElasticsearchRepository<CandidateDocument, String> {
}
