use gene_linkage;

load data local infile '/data/genelinkage/sql/article_score.sql'
into table article_score
ignore 1 lines;

load data local infile '/data/genelinkage/sql/gene_article_score.sql'
into table gene_article_score
ignore 1 lines;

load data local infile '/data/genelinkage/sql/gene_article_mention.sql'
into table gene_article_mention
ignore 1 lines;
