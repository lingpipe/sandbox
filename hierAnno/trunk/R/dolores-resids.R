source("dolores-sensSpec.R")
pdf(file="dolores-beta-binomial-resid-sens.pdf",
    onefile=FALSE)
plot(sens,sens,
     xlab="MLE gold standard sensitivity",ylab="estimated sensitivity theta_1",
     main="80% Interval Estimates of Sensitivity",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J)
  lines(sens[j] * c(1,1),quantile(theta.1[,j],c(0.1,0.9)))
abline(h=median(acc.1))
dev.off()

pdf(file="dolores-beta-binomial-resid-spec.pdf",
    onefile=FALSE)
plot(spec,spec,
     xlab="MLE gold standard specificity",
     ylab="estimated specificity theta_0",
     main="80% Interval Estimates of Specificity",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J)
  lines(spec[j] * c(1,1),quantile(theta.0[,j],c(0.1,0.9)))
abline(h=median(acc.0))
dev.off()

