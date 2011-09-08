To recreate our submissions, first setwd() into this
directory, then:

Semi-supervised
---------------

In R, generate submission:

> setwd("trec2011/R")
> source("model_semisup.R")  # fit model
> source("submission.R")     # generate submission files

In the shell, check validity:

% cd trec2011/script
% perl check_crowd2.pl ../submissions/lingpipe_semisup_binary.csv
% perl check_crowd2.pl ../submissions/lingpipe_semisup.csv

Back in R for graphs:

> source("sens_spec.R")
> pdf(file="../paper/img/sens_vs_spec_hat_2.pdf",width=4,height=4);
> p;
> dev.off();



Unsupervised
------------
