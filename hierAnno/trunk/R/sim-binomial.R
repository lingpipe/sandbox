J <- 20
I <- 1000

sim.pi <- 0.2
sim.c <- rbinom(I,1,sim.pi)

sim.theta.0 <- 40/48 # to match betas
sim.theta.1 <- 20/28 # to match betas

x <- matrix(0,I,J)
for (i in 1:I) {
  for (j in 1:J) {
    x[i,j] <- rbinom(1,1,sim.c[i]*sim.theta.1 + (1-sim.c[i])*(1-sim.theta.0))
  }
}

source("censor.R")

source("sparsify.R")

