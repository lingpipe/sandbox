library("R2WinBUGS")

data <- list("I","J","K","xx","ii","jj")

parameters <- c("c",
                "pi","theta.0","theta.1",
                "alpha.0", "beta.0", "acc.0", "scale.0",
                "alpha.1", "beta.1", "acc.1", "scale.1")

inits <- function() {
  list(pi=runif(1,0.7,0.8),
#       c=rbinom(I,1,0.5),
       acc.0 <- runif(1,0.9,0.9),
       scale.0 <- runif(1,5,5),
       acc.1 <- runif(1,0.9,0.9),
       scale.1 <- runif(1,5,5),
       theta.0=runif(J,0.9,0.9),
       theta.1=runif(J,0.9,0.9)
       )
}

anno <- bugs(data, inits, parameters,
             "c:/carp/devguard/sandbox/hierAnno/trunk/R/bugs/beta-binomial-anno.bug",
              n.chains=3, n.iter=500,
#              n.thin=5,
              debug=TRUE,
              clearWD=TRUE,
              bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(anno)
plot(anno)
attach.bugs(anno)

