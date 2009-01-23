library("R2WinBUGS")

data <- list("I","K","xx","ii")

parameters <- c("pi","theta", # "c"
                "alpha.0", "beta.0", "acc.0", "scale.0",
                "alpha.1", "beta.1", "acc.1", "scale.1")

inits <- function() {
  list(pi=runif(1,0.4,0.6),
       c=rbinom(I,1,0.5),
       acc.0 <- runif(1,0.8,0.9),
       scale.0 <- runif(1,20,30),
       acc.1 <- runif(1,0.7,0.8),
       scale.1 <- runif(1,10,20),
       theta=runif(I,0.8,0.9))
}

anno <- bugs(data, inits, parameters,
             "c:/carp/sandbox/hierAnno/R/bugs/beta-binomial-item.bug",
              n.chains=3, n.iter=100,
              debug=TRUE,
              clearWD=TRUE,
              bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(anno)
plot(anno)
attach.bugs(anno)

