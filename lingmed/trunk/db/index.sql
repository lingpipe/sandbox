use gene_linkage;

alter table gene_article_score add index entrezgene_score_index (entrezgene_id);
desc gene_article_score;

alter table gene_article_mention add index entrezgene_mention_index (entrezgene_id);
desc gene_article_mention;

alter table gene_article_score add index score_index (pubmed_id,entrezgene_id);
desc gene_article_score;

alter table gene_article_mention add index mention_index (pubmed_id,entrezgene_id);
desc gene_article_mention;
