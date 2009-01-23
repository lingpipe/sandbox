library("R2WinBUGS")

data <- list("I","J","K","xx","ii","jj")

parameters <- c("pi", "c",
                "beta",
                # "mu.beta.0", "sigma.beta.0",
                "mu.beta.1", # "sigma.beta.1",
                "alpha",
                "mu.alpha", "sigma.alpha",
                "delta")


source("majority-cats-sparse.R")

inits <- function() {
  list(pi=0.5,
       c=c.maj, # c=rbinom(I,1,0.5),
       mu.beta.1=4,
       # sigma.beta.1=1,
       alpha=rep(2,J),
       mu.alpha=2,
       sigma.alpha=1,
       delta=rep(1,J))
}

anno <- bugs(data, inits, parameters,
             "c:/carp/sandbox/hierAnno/R/bugs/uebersax-grove.bug",
              n.chains=3, n.iter=500,
              debug=TRUE,
              clearWD=TRUE,
              bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(anno)
plot(anno)
attach.bugs(anno)

