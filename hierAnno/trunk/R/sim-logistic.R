library("arm")
J <- 40
I <- 2000
sim.pi <- 0.20
sim.c <- rbinom(I,1,sim.pi)

sim.mu.0 <- 2
sim.sigma.0 <- 1.25
sim.gamma.0 <- rnorm(J,sim.mu.0,sim.sigma.0)

sim.mu.1 <- 1
sim.sigma.1 <- 0.5
sim.gamma.1 <- rnorm(J,sim.mu.1,sim.sigma.1)

sim.rho.0 <- 0.5
sim.rho.1 <- 1.0

sim.delta <- rep(NA,I)
for (i in 1:I) {
  if (sim.c[i] == 0) {
    sim.delta[i] <- rnorm(1,0,sim.rho.0)
  } else {
    sim.delta[i] <- rnorm(1,0,sim.rho.1)
  }
}

x <- matrix(data=NA,I,J)
for (i in 1:I) {
  for (j in 1:J) {
    x[i,j] <- rbinom(1,1,invlogit(sim.c[i]*(sim.gamma.1[j] - sim.delta[i]) + (1 - sim.c[i]) * (sim.delta[i] - sim.gamma.0[j])))
  }
}

source("censor.R")
source("sparsify.R")

