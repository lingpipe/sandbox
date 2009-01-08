load data infile "C:\\mitzi\\cvsroot\\lingmed\\db\\gl_sql\\article_score.sql"
into table article_score
ignore 1 lines;

load data infile "C:\\mitzi\\cvsroot\\lingmed\\db\\gl_sql\\gene_article_score.sql"
into table gene_article_score
ignore 1 lines;

load data infile "C:\\mitzi\\cvsroot\\lingmed\\db\\gl_sql\\gene_article_mention.sql"
into table gene_article_mention
ignore 1 lines;
