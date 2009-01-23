source("dolores-sensSpec.R")

pdf(file="dolores-rte-resids2D.pdf",
    onefile=FALSE)
sqrtCounts <- rep(NA,J)
for (j in 1:J)
  sqrtCounts[j] <- sqrt(sum(jj==j))/3
plot(sens,spec,col="red",pch=1,cex=sqrtCounts,xlim=c(0,1),ylim=c(0,1),
     xlab="sensitivity: theta_1",
     ylab="specificity: theta_0",
     main="Estimated vs. Gold-Standard Specificity and Sensitivity",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
abline(1,-1,col="green")  # random performance
abline(v=median(acc.1),col="blue")
abline(h=median(acc.0),col="blue")
#plot(sens.hat,spec.hat,col="blue",new=FALSE)
for (j in 1:J)
  lines(c(sens[j],sens.hat[j]),c(spec[j],spec.hat[j]),col="gray")
dev.off()