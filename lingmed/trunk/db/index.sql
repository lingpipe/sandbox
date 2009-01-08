alter table gene_article_score add index entrezgene_score_index (entrezgene_id);

alter table gene_article_mention add index entrezgene_mention_index (entrezgene_id);

alter table gene_article_score add index score_index (pubmed_id,entrezgene_id);

alter table gene_article_mention add index mention_index (pubmed_id,entrezgene_id);
