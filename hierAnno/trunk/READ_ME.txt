LingPipe Sandbox Project:  hierAnno
======================================================================

There is both R/BUGS code and Java code in this sandbox project to do
automatic inference of gold-standard categories, annotator
sensitivities and specificities, category prevalence, and hierarchical
priors for sensitivity and specificity.  It also contains the LaTeX
source for the tech report and Mechanical Turk meetup presentation for
the model.

Complete definitions and evaluations of the models implemented in this
project may be found in:

  Carpenter, Bob. 2008. Multilevel Bayesian Models of Categorical Data Annotation</a>. 
  Technical Report. Alias-i.

  http://lingpipe.files.wordpress.com/2008/11/carp-bayesian-multilevel-annotation.pdf



JAVA
-----------------------------------------------

The Java programs may be compiled and run through ant in the usual
way.  Compiling the javadoc will provide API level advice.  The
command package has information on running the estimators from
the command line.


R/BUGS
-----------------------------------------------

The subdirectory /R contains R code for running models and plotting
outputs.  This was the main repository for scripts used to generate
evaluation results and plots for the various papers.  

The subdirectory /R/data contains data from various sources for
annotation models.  This includes the Dolores Labs RTE data and
the MUC named-entity data we collected from Mechanical Turk, as
well as a number of standard epidemiology data sets like Handelman's
dentistry data.

The subdirectory /R/bugs contains BUGS model specifications for
the various models.

In general, once R is loaded, the appropriate R script needs to
be called to load the data.   Once the data's loaded, one of the
top-level R scripts may be called to run the BUGS models from
R.  This presupposes the R/BUGS interface package has been set up.

BUGS/R interface help:  http://www.stat.columbia.edu/~gelman/bugsR/


LATEX
-----------------------------------------------

The paper LaTeX source code is available along with all the linked
graphics.  It's all set up to run with pdflatex from the directory in
which the .tex files are found.

