/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.ai.example.rag.service;

import com.alibaba.cloud.ai.document.DocumentWithScore;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import com.alibaba.cloud.ai.rag.retrieval.search.HybridElasticsearchRetriever;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Retrieve documents via hybrid retriever then rerank using DashScope rerank model.
 */
@Service
public class RerankService {

    private final HybridElasticsearchRetriever hybridRetriever;
    private final RerankModel rerankModel;

    public RerankService(HybridElasticsearchRetriever hybridRetriever,
                         RerankModel rerankModel) {
        this.hybridRetriever = hybridRetriever;
        this.rerankModel = rerankModel;
    }

    /**
     * Hybrid retrieve then rerank; metadata.rerank_score carries rerank score.
     */
    public List<Document> retrieveAndRerank(String queryText) {
        Query query = Query.builder().text(queryText).build();
        List<Document> retrieved = hybridRetriever.retrieve(query);
        RerankResponse resp = rerankModel.call(new RerankRequest(queryText, retrieved));
        return resp.getResults().stream().map(this::mergeScore).toList();
    }

    private Document mergeScore(DocumentWithScore dws) {
        Document doc = dws.getOutput();
        Map<String, Object> metadata = doc.getMetadata();
        Map<String, Object> mergedMetadata =new HashMap<>(metadata);
        mergedMetadata.put("rerank_score", dws.getScore());
        return doc.mutate().metadata(mergedMetadata).build();
    }
}
