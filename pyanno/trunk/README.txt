README: pyanno 1.0
============================================================

What is pyanno?
------------------------------------------------------------ 

pyanno 1.0 is a suite of Python libraries for modeling the data coding
or diagnostic testing process to support inferences required for data
curation.  Specifically, pyanno implements statistical models for
inferring from multiply coded categorical data 

    * annotator accuracies and biases,
    * gold standard categories of items,
    * prevalence of categories in population, and
    * population distribution of annotator accuracies and biases.

The models include Dawid and Skene's (1979) multinomial model with a
maximum likelihood estimator implemented with EM.  A generalization of
Dawid and Skene's model with Dirichlet priors on prevalence and
estimator accuracy is also implemented.  For this model, estimator
implementations include a maximum a posteriori estimator via
expectation maximization (EM), a Bayesian posterior sampler estimated
via Gibbs sampling, and a Bayesian estimator implemented via averaging
Gibbs samples from the posterior.


Licensing
------------------------------------------------------------
PyCurate is licensed under the Apache License, Version 2.0.
See License.txt for more information.


Installation and Use
------------------------------------------------------------
Instructions for installing and Using PyCurate are
in Documentation.txt.


Contributors
------------------------------------------------------------
Bob Carpenter    (Columbia University,   Statistics)
Andrey Rzhetsky  (University of Chicago, Medicine)
James Evans      (University of Chicago, Sociology)


References
------------------------------------------------------------

Dawid, A. P. and A. M. Skene. 1979. Maximum likelihood estimation of
observed error rates using the EM algorithm. Journal of Applied
Statistics 28(1):20--28.

Rzhetsky A., H. Shatkay, and W. J. Wilbur. 2009. How to get the most
out of your curation effort. PLoS Computational Biology 5(5).
