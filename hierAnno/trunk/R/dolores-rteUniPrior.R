setwd("c:\\carp\\mycvs\\carp\\papers\\dolores\\R");

library("R2WinBUGS")

# source("data.R")

# Enforces a Uniform Prior equiv to alpha == beta == 1

acc.0 <- 0.5
scale.0 <- 2

acc.1 <- 0.5
scale.1 <- 2


c.maj=rep(NA,I)
for (i in 1:I) {
  vote <- mean(xx[ii==i & is.finite(xx)])
  if (vote == 0.5)
    c.maj[i] = rbinom(1,1,0.5)
  else if (vote > 0.5)
    c.maj[i] = 1
  else
    c.maj[i] = 0
}


data <- list("I","J","K","ii","jj","xx",
             "acc.0","scale.0","acc.1","scale.1")
parameters <- c("q","c",
                "a.0","a.1")

inits <- function() {
  list(q=runif(1,0.25,0.75),
       a.0=runif(J,0.60,0.95),
       a.1=runif(J,0.60,0.95),
       c=c.maj) # rbinom(I,1,0.5))
}

setwd("c:\\carp\\mycvs\\carp\\papers\\hierAnno\\R\\models\\model4")

rte.bugs <- bugs(data, inits, parameters,
               "anno.4.bug",
               n.chains=3, n.iter=100,
               debug=TRUE,
               clearWD=TRUE,
               bugs.directory="c:\\WinBUGS\\WinBUGS14")

print(rte.bugs)
plot(rte.bugs)

attach.bugs(rte.bugs)

setwd("c:\\carp\\mycvs\\carp\\papers\\dolores\\R");

source("sensSpec.R")



