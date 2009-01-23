library("R2WinBUGS")

data <- list("I","J","K","xx","ii","jj")

parameters <- c("pi", "c",
                "gamma.0", "gamma.1",
                "delta",
                "mu.0", "mu.1",
                "sigma.0", "sigma.1",
                "rho.0", "rho.1")


# source("majority-cats-sparse.R")

inits <- function() {
  list(pi=runif(1,0.20,0.20),
#       c=rbinom(I,1,0.5),
       gamma.0=runif(J,2,2),
       gamma.1=runif(J,3,3),
       delta=runif(I,0,0),
       mu.0=runif(1,2,2),
       mu.1=runif(1,1,1),
       sigma.0=runif(1,1,1),
       sigma.1=runif(1,1,1),
       rho.0=runif(1,1,1),
       rho.1=runif(1,1,1))
}

anno <- bugs(data, inits, parameters,
             "c:/carp/sandbox/hierAnno/R/bugs/logistic.bug",
              n.chains=3, n.iter=1000,
              debug=TRUE,
              clearWD=TRUE,
              bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(anno)
plot(anno)
attach.bugs(anno)

