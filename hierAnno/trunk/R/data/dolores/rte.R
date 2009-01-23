rte.tsv <- scan("data/dolores/rte.data.tsv")
K <- 8000
mat.rte <- matrix(rte.tsv,nrow=K,ncol=3,byrow=TRUE)
ii <- mat.rte[,1]
jj <- mat.rte[,2]
xx <- mat.rte[,3]
I <- max(ii)
J <- max(jj)

c.gold <- scan("data/dolores/rte.gold.tsv")  # output of running MungeRte on distributed .tsv

# c <- c.gold
