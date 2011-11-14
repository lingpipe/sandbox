# install.packages("rjags");
# install.packages("R2jags");

library("rjags");
library("R2jags");

rawDataTable <- read.table('../../data/fair_5ann_all.txt',header=T,na.strings="\\N");
I <- dim(rawDataTable)[1];
J <- dim(rawDataTable)[2];
itemId <- rownames(rawDataTable);
annoId <- colnames(rawDataTable);
senseId <- levels(as.factor(as.vector(as.matrix(rawDataTable))));
K <- length(senseId);
senseIdToIdx <- rep(0,1000);
for (k in 1:K)
  senseIdToIdx[as.integer(senseId[k])] = k;

N <- sum(!is.na(as.vector(as.matrix(rawDataTable))));
ii <- rep(0,N);
jj <- rep(0,N);
y  <- rep(0,N);
n <- 1;
for (i in 1:I) {
  for (j in 1:J) {
    if (!is.na(rawDataTable[i,j])) {
      ii[n] <- i;
      jj[n] <- j;
      y[n] = senseIdToIdx[rawDataTable[i,j]];
      n <- n + 1;
    }
  }
}
alpha <- rep(1,K);  # uniform
beta <- matrix(1,K,K);
# weakly informative accuracy; remove loop for uniform
for (k in 1:K) {
  # prior mean: acc = K/(K+K-1) ~ 1/2
  beta[k,k] = K; 
}


votes <- matrix(0,I,K);
piVote <- rep(0,K);
for (n in 1:N) {
  votes[ii[n],y[n]] <- votes[ii[n],y[n]] + 1;
  piVote[y[n]] <- piVote[y[n]] + 1;
}
piVote <- piVote / sum(piVote)
zVote <- rep(1,I);
for (i in 1:I) {
  for (k in 2:K) {
    numTies <- 0;
    if (votes[i,k] > votes[i,zVote[i]]) {
      zVote[i] <- k;
    } else if (votes[i,k] == votes[i,zVote[i]]) {
      numTies <- numTies + 1;
      # keep track of numTies so random choice isn't biased
      zVote[i] <- ifelse(rbinom(1,1,1.0/(1.0 + numTies)),k,zVote[i]);
    }
  }
}

jagsData <- list("I", "J", "K", "N", "ii", "jj", "y", "alpha","beta");
jagsParams <- c("pi","z","theta");
jagsInits <- function() { list("z"=zVote,
                               "pi"=piVote); }
jagsFit <- jags(data=jagsData, inits=jagsInits,
                parameters.to.save=jagsParams,
                n.iter=2000, n.chains=4, 
                model.file="basic.jags");
print(jagsFit);
attach.jags(jagsFit);



