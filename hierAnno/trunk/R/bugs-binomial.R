library("R2WinBUGS")
data <- list("I","K","xx","ii")
parameters <- c("c","pi","theta.0","theta.1") # ,"c")

inits <- function() {
  list(pi=runif(1,0.4,0.6),
       c=rbinom(I,1,0.5),
       theta.0=runif(1,0.7,0.8),
       theta.1=runif(1,0.7,0.8))
}
anno <- bugs(data, inits, parameters,
             "c:/carp/sandbox/hierAnno/R/bugs/binomial.bug",
              n.chains=3, n.iter=1000,
              debug=TRUE,
              clearWD=TRUE,
              bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(anno)
plot(anno)
attach.bugs(anno)

