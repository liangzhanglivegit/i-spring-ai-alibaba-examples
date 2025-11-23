# Spring AI Alibaba RAG Elasticsearch Example ###

This section will describe how to create example and use rag component. 

## Quick Start

### Use the RAG component

### Hybrid retrieve + rerank 示例

本示例演示：先用 `HybridElasticsearchRetriever` 做混合检索，再用 DashScope `RerankModel` 重排，重排得分写入返回文档的 `metadata.rerank_score`，并封装为可复用的 Service。

- Service：`com.alibaba.cloud.ai.example.rag.service.RerankService#retrieveAndRerank(String queryText)`  
  - 内部：`hybridRetriever.retrieve(query)` -> `rerankModel.call(...)` -> 将 `rerank_score` 写回 metadata。
- 控制器：`GET /rag/rerank/hybrid?query=xxx` 返回带 `metadata.rerank_score` 的文档列表。

调用示例（HTTP）：

```
GET http://localhost:8080/rag/rerank/hybrid?query=什么是hybridSearch
```

返回的每个 `Document` 中 `metadata.rerank_score` 即为重排分，可直接给前端或后续流水线使用。

