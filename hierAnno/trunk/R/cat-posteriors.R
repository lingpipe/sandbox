pdf(file="dentistry-beta-binomial-anno-post-cats.pdf",
    width=16, height=8,
    onefile=FALSE)
bools <- matrix(NA,2**J,J)
for (j in 1:J)
  bools[,j] <- rep(c(rep(0,2**(J-j)), rep(1,2**(J-j))), 2**(j-1))

num.samples <- length(pi)
par(mfrow=c(4,8))
for (k in 1:2**J) {
  p.0 <- rep(NA,num.samples)
  p.1 <- rep(NA,num.samples)
  p <- rep(NA,num.samples)
  for (samp in 1:num.samples) {
    p.0[samp] <- (1 - pi[samp])
    p.1[samp] <- pi[samp]
    for (j in 1:J) {
      if (bools[k,j] == 1) {
        p.0[samp] <- p.0[samp] * (1 - theta.0[samp,j])
        p.1[samp] <- p.1[samp] * theta.1[samp,j]
     } else {
        p.0[samp] <- p.0[samp] * theta.0[samp,j]
        p.1[samp] <- p.1[samp] * (1 - theta.1[samp,j])
      }
    }
    p[samp] <- p.1[samp] / (p.0[samp] + p.1[samp])
  }

  hist(p,xlim=c(0,1), ylim=c(0,10),breaks=seq(from=0,to=1.0,by=0.01),
       freq=FALSE,
       axes=FALSE, ylab=NULL, xlab=NULL,
       main=paste(bools[k,],collapse=""))
  axis(side=1, labels=c("0.0","","0.5","","1.0"),at=c(0,0.25,0.5,0.75,1.0))
}
dev.off()



