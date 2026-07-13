package com.luontd.etlworkerservice.infrastructure.search.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data Elasticsearch repository cho CandidateDocument.
 * Cung cấp save/findById/delete OOTB.
 */
@Repository
public interface CandidateSearchRepository extends ElasticsearchRepository<CandidateDocument, String> {
}
