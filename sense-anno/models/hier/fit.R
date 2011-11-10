# install.packages("rjags");
# install.packages("R2jags");

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
    if (!is.na(x[i,j])) {
      ii[n] <- i;
      jj[n] <- j;
      y[n] = senseIdToIdx[x[i,j]];
      n <- n + 1;
    }
  }
}
alpha <- rep(1,K);

jagsData <- list("I", "J", "K", "N", "ii", "jj", "y", "alpha");
jagsParams <- c("pi","z","theta");
jagsInits <- function() { list(); }
jagsFit <- jags(data=jagsData, inits=jagsInits, n.iter=100,
                model.file="hier.jags");
print(jagsFit);





                      
