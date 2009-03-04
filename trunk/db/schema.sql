use annotation_wiki;

CREATE TABLE  article (
	id INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	pubmed_id INTEGER UNSIGNED NOT NULL,
	ordering INTEGER UNSIGNED NOT NULL
) ENGINE=InnoDB;

CREATE TABLE  entity (
	id INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	source_id VARCHAR(4) NOT NULL,
	entity_id VARCHAR(80) NOT NULL
) CHARACTER SET utf8 ENGINE=InnoDB;


CREATE TABLE web_user (
	id INTEGER UNSIGNED AUTO_INCREMENT PRIMARY KEY,
	name VARCHAR(80) UNIQUE NOT NULL
) ENGINE=InnoDB;


CREATE TABLE  annotation (
	article_id INTEGER UNSIGNED NOT NULL,
	entity_id INTEGER UNSIGNED NOT NULL,
	user_id INTEGER UNSIGNED NOT NULL,
	FOREIGN KEY (article_id) REFERENCES article(id),
	FOREIGN KEY (entity_id) REFERENCES entity(id),
	FOREIGN KEY (user_id) REFERENCES web_user(id)
) ENGINE=InnoDB;

CREATE TABLE user_article_count (
	user_id INTEGER UNSIGNED NOT NULL,
	article_id INTEGER UNSIGNED NOT NULL,
	ct_entities INTEGER UNSIGNED NOT NULL DEFAULT 0,
	FOREIGN KEY (article_id) REFERENCES article(id),
	FOREIGN KEY (user_id) REFERENCES web_user(id),
	UNIQUE KEY (user_id, article_id)
) ENGINE=InnoDB;

