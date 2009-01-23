J <- 20
I <- 1000
sim.pi <- 0.20
sim.c <- rbinom(I,1,sim.pi)

sim.alpha.0 <- 40
sim.beta.0 <- 8
sim.scale.0 <- sim.alpha.0 + sim.beta.0
sim.acc.0 <- sim.alpha.0 / sim.scale.0
sim.theta.0 <- rbeta(J,sim.alpha.0,sim.beta.0)

sim.alpha.1 <- 20
sim.beta.1 <- 8
sim.scale.1 <- sim.alpha.1 + sim.beta.1
sim.acc.1 <- sim.alpha.1/sim.scale.1
sim.theta.1 <- rbeta(J,sim.alpha.1,sim.beta.1)

x <- matrix(data=NA,I,J)
for (i in 1:I) {
  for (j in 1:J) {
    x[i,j] <- rbinom(1,1,
                     sim.c[i] * sim.theta.1[j]
                     + (1-sim.c[i]) * (1 - sim.theta.0[j]))
  }
}

source("censor.R")
source("sparsify.R")

