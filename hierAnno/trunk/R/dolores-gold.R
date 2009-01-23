resids <- rep(NA,I)
for (i in 1:I)
  resids[i] <- (c.gold[i] - mean(c[,i]))

pdf(file="dolores-cat-resids-model-pruned.pdf",
    onefile=FALSE)
hist(resids,xlim=c(-1,1),ylim=c(0,800),col="blue",
     main="Pruned Model Residual Category Error",
     xlab="residual error", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()


voted.resids <- rep(NA,I)
for (i in 1:I)
  voted.resids[i] <- (c.gold[i] - mean(xx[ii == i & is.finite(xx)]))

pdf(file="dolores-cat-resids-voted-pruned.pdf",
    onefile=FALSE)
hist(voted.resids,breaks=10,xlim=c(-1,1),ylim=c(0,800),col="blue",
     main="Pruned Voting Residual Category Error",
     xlab="residual error", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

# display IDs of residual errors
for (i in 1:I)
  if (abs(c.gold[i] - mean(c[,i])) >= 0.5)
    print(i)


print(c("|resid| >= 0.5",sum(abs(resids) >= 0.5)))
gt5 <- sum(abs(voted.resids) > 0.5)
print(c("|majority.resid| > 0.5",gt5))
eq5 <- sum(abs(voted.resids) == 0.5)
print(c("|majority.resid| == 0.5",eq5))
print(c("majority exp errors=",gt5 + eq5/2))
