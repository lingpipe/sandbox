rte.raw <- scan("data/mturk-ne/per.anno.tsv")
K <- length(rte.raw)/3
mat.rte <- matrix(rte.raw,nrow=K,ncol=3,byrow=TRUE)
ii <- mat.rte[,1]
jj <- mat.rte[,2]
xx <- mat.rte[,3]
I <- max(ii)
J <- max(jj)
K <- length(ii)


c.copper <- scan("data/mturk-ne/per.gold.tsv")  # -1 for non-caps

c.gold <- rep(0,length(c.copper))
c.gold[c.copper==1] <- 1

