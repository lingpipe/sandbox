drop table if exists gene_article_mention;

drop table if exists article_score;
create table article_score (
	pubmed_id INTEGER UNSIGNED PRIMARY KEY,
	genomics_score DOUBLE NOT NULL
) ENGINE=InnoDB;
desc article_score;

drop table if exists gene_article_score;
create table gene_article_score (
	entrezgene_id INTEGER UNSIGNED NOT NULL,
	pubmed_id INTEGER UNSIGNED NOT NULL,
	per_gene_score DOUBLE NOT NULL,
	total_score DOUBLE NOT NULL
) ENGINE=InnoDB;
desc gene_article_score;

CREATE TABLE  gene_article_mention (
	entrezgene_id INTEGER UNSIGNED NOT NULL,
	pubmed_id INTEGER UNSIGNED NOT NULL,
	text_matched VARCHAR(256) NOT NULL,
	start_offset INTEGER UNSIGNED NOT NULL
) ENGINE=InnoDB;
desc gene_article_mention;

