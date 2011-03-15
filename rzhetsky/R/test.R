library("MCMCpack")
library("rjags")
library("R2jags")

K = 3;
J = 20;
I = 2000;
alpha = rep(3,K);
pi = rdirichlet(1,alpha);
beta = array(dim=c(K,K));
for (k in 1:K) {
    beta[k,1:K] = rep(1,K);
    beta[k,k] = K;
}
z = sample.int(K,prob=pi,replace=TRUE,size=I);
theta = array(dim=c(J,K,K))
for (k in 1:K) {
    theta[1:J,k,1:K] = rdirichlet(J,beta[k,1:K]);
}
phi = 0.5
n = 0;
ii = array(dim=c(I*J))
jj = array(dim=c(I*J))
y = array(dim=c(I*J))
for (i in 1:I) {
    for (j in 1:J) {
        if (rbinom(1,1,0.5) == 1) {
            n = n + 1;
            ii[n] = i;
            jj[n] = j;
            y[n] = sample.int(K,prob=theta[j,z[ii[n]],1:K],size=1);
        }
    }
}
N = n;
ii = array(ii,dim=c(N))
jj = array(jj,dim=c(N))
y = array(y,dim=c(N))

test.data <- list("I","J","K","N","ii","jj","alpha","beta","y");
test.params <- c("pi","theta","z");
test.inits <- function() {
    list(pi=pi,
         theta=theta,
         z=z)
}
test.fit =
    jags(model.file="multinom.jags",
         data=test.data,
         inits=NULL,
	 parameters.to.save=test.params,
         n.iter=50,
         n.chains=1,
         DIC=FALSE,
         n.thin=1,
         n.burnin=1);
         

