votes.0 <- rep(0,I)
votes.1 <- rep(0,I)
for (k in 1:K) {
  if (xx[k] == 0) {
    votes.0[ii[k]] <- votes.0[ii[k]] + 1
  } else {
    votes.1[ii[k]] <- votes.1[ii[k]] + 1
  }
}

est.voted <- rep(NA,I)
for (i in 1:I)
  est.voted[i] <- votes.1[i] / (votes.0[i] + votes.1[i])

hist(est.voted)

voted.resids <- rep(NA,I)
for (i in 1:I) {
  voted.resids[i] <- (c.gold[i] - est.voted[i])
  if (abs(voted.resids[i]) > 0.5)
    print(c(i,voted.resids[i]))
}

hist(voted.resids,breaks=10,xlim=c(-1,1),col="blue",
     main="Pruned Voting Residual Category Error",
     xlab="residual error", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)


