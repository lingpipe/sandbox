spec <- rep(NA,J)
for (j in 1:J)
  spec[j] <- sum(jj == j & c.gold[ii] == 0 & xx == 0) / sum(jj==j & c.gold[ii] == 0)

sens <- rep(NA,J)
for (j in 1:J)
  sens[j] <- sum(jj == j & c.gold[ii] == 1 & xx == 1) / sum(jj==j & c.gold[ii] == 1)

for (j in 1:J) {
  if (!is.finite(spec[j])) spec[j] = 1
  if (!is.finite(sens[j])) sens[j] = 1
  print(c(sens[j],spec[j]))
}


spec.hat <- rep(NA,J)
for (j in 1:J)
  spec.hat[j] <- mean(theta.0[,j])

sens.hat <- rep(NA,J)
for (j in 1:J)
  sens.hat[j] <- mean(theta.1[,j])


sqrtCounts <- rep(NA,J)
for (j in 1:J)
  sqrtCounts[j] <- sqrt(sum(jj==j))/5

pdf(file="ne-turk-sens-spec-2D.pdf",
    onefile=FALSE)
plot(sens[1:50],spec[1:50],col="red",pch=1,cex=sqrtCounts,xlim=c(0,1),ylim=c(0,1),
     xlab="sensitivity: theta_1",
     ylab="specificity: theta_0",
     main="Posterior vs. Gold Standard Estimates",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)

abline(1,-1,col="green")  # random performance
abline(v=median(acc.1),col="blue")
abline(h=median(acc.0),col="blue")
for (j in 1:50)
 lines(c(sens[j],sens.hat[j]),c(spec[j],spec.hat[j]),col="gray")
dev.off()


pdf(file="ne-turk-sens-spec-2D-b.pdf",
    onefile=FALSE)
plot(sens,spec,col="red",pch=1,cex=sqrtCounts,xlim=c(0.75,1),ylim=c(0.75,1),
     xlab="sensitivity: theta_1",
     ylab="specificity: theta_0",
     main="Posterior vs. Gold Standard Estimates",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)

abline(1,-1,col="green")  # random performance
abline(v=median(acc.1),col="blue")
abline(h=median(acc.0),col="blue")
dev.off()

