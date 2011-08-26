library("rjags");
library("R2jags");

mu <- 1.0;
tau <- 0.25;
N <- 5;

jags.data <- list("mu","tau","N");

jags.params <- c("y");

jags.inits <- function() {
  list(y=rnorm(N,mu,tau));
}

jags.fit <- jags(data=jags.data, inits=jags.inits, jags.params,
                 n.iter=100, model.file="../jags/demo.jags",
                 DIC=FALSE);

print(jags.fit);
plot(jags.fit);
attach(jags.fit);
