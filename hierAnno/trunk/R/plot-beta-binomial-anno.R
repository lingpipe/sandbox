pdf(file="beta-binomial-anno-posterior-pi.pdf")
hist(pi,
     breaks=seq(0.15:0.25,by=0.005),
     xlim=c(0.15,0.25), ylim=c(0,250),
     main="Posterior: pi",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-anno-sim-thetas.pdf", onefile=FALSE)
plot(sim.theta.0,sim.theta.1,
     xlim=c(0.5,1.0), ylim=c(0.5,1.0),
     pch=20,
     main="Simulated theta.0 & theta.1",
     xlab="theta.0", ylab="theta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
v = sim.alpha.0/(sim.alpha.0 + sim.beta.0)
h = sim.alpha.1/(sim.alpha.1 + sim.beta.1)
abline(h=h,col="lightgrey")
abline(v=v,col="lightgrey")
dev.off()


pdf(file="beta-binomial-anno-scatter-spec.pdf",
    onefile=FALSE)
plot(acc.0,scale.0,xlim=c(0.60,0.90),ylim=c(0,100),pch=20,
     main="Posterior: Specificity Mean & Scale",
     xlab="alpha.0 / (alpha.0 + beta.0)",
     ylab="alpha.0 + beta.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.0 + sim.beta.0
abline(h=h)
v = sim.alpha.0/(sim.alpha.0 + sim.beta.0)
abline(v=v)
dev.off()

pdf(file="beta-binomial-anno-scatter-sens.pdf",
    onefile=FALSE)
plot(acc.1,scale.1,xlim=c(0.60,0.90),ylim=c(0,100),pch=20,
     main="Posterior: Sensitivity Mean & Scale",
     xlab="alpha.1 / (alpha.1 + beta.1)",
     ylab="alpha.1 + beta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.1 + sim.beta.1
abline(h=h)
v = sim.alpha.1/(sim.alpha.1 + sim.beta.1)
abline(v=v)
dev.off()

pdf(file="beta-binomial-anno-theta0-fit.pdf",
    onefile=FALSE)
mean.theta.0 <- rep(NA,J)
for (j in 1:J) mean.theta.0[j] <- mean(theta.0[,j])
plot(sim.theta.0,mean.theta.0,
     xlim=c(0.5,1.0), ylim=c(0.5,1.0),
     main="Estimated vs. Simulated theta.0",
     xlab="simulated theta.0",
     ylab="mean estimate theta.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J)
  lines(sim.theta.0[j] * c(1,1),quantile(theta.0[,j],c(0.025,0.975)))
abline(coef=c(0,1), col="lightgrey")
dev.off()

pdf(file="beta-binomial-anno-theta1-fit.pdf",
    onefile=FALSE)
mean.theta.1 <- rep(NA,J)
for (j in 1:J) mean.theta.1[j] <- mean(theta.1[,j])
plot(sim.theta.1,mean.theta.1,
     xlim=c(0.5,1.0), ylim=c(0.5,1.0),
     main="Estimated vs. Simulated theta.1",
     xlab="simulated theta.1",
     ylab="mean estimate theta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J)
  lines(sim.theta.1[j] * c(1,1),quantile(theta.1[,j],c(0.025,0.975)))
abline(coef=c(0,1), col="lightgrey")
dev.off()



pdf(file="beta-binomial-anno-post-theta0.pdf", onefile=FALSE)
hist(theta.0[,3],
     breaks=seq(0.4,1.0,0.01),
     xlim=c(0.4,1.0), ylim=c(0,400),
     pch=20,
     main="Posterior: theta.0[3]",
     xlab="theta.0", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-anno-post-theta1.pdf", onefile=FALSE)
hist(theta.1[,3],
     breaks=seq(0.4,1.0,0.01),
     xlim=c(0.4,1.0), ylim=c(0,400),
     pch=20,
     main="Posterior: theta.1[3]",
     xlab="theta.1", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-anno-cat-residual.pdf",
    onefile=FALSE)
cat.resid <- rep(NA,I)
for (i in 1:I)
  cat.resid[i] <- sim.c[i]-mean(c[,i])
h1 <- hist(cat.resid,breaks=seq(-1,1,0.05),xlim=c(-1,1))
h1$counts <- log(h1$counts + 1)/log(2)
plot(h1,
     main="Residual Category Error",
     ylab="log2(freq+1)", xlab="residual category error",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

