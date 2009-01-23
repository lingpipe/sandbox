pdf(file="logistic-cat-residual.pdf",
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


pdf(file="logistic-posterior-pi.pdf")
hist(pi,
     breaks=seq(0.14:0.22,by=0.005),
     xlim=c(0.14,0.22), ylim=c(0,350),
     main="Posterior: pi",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="logistic-scatter-spec.pdf",
    onefile=FALSE)
plot(mu.0,sigma.0,pch=20,xlim=c(0,3),ylim=c(0,2),
     main="Posterior: Specificity Mean & Deviation",
     xlab="mu.0",
     ylab="sigma.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.sigma.0
abline(h=h)
v = sim.mu.0
abline(v=v)
dev.off()

pdf(file="logistic-scatter-sens.pdf",
    onefile=FALSE)
plot(mu.1,sigma.1,pch=20,xlim=c(0,3),ylim=c(0,2),
     main="Posterior: Sensitivity Mean & Deviation",
     xlab="mu.1",
     ylab="sigma.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.sigma.1
abline(h=h)
v = sim.mu.1
abline(v=v)
dev.off()

pdf(file="logistic-post-rho_0.pdf",
    onefile=FALSE)
hist(rho.0,xlim=c(0.3,1.3),ylim=c(0,600),breaks=seq(0.3,1.3,0.025),
     main="Posterior: Difficulty Deviation, c=0",
     xlab="rho.0",
     ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="logistic-post-rho_1.pdf",
    onefile=FALSE)
hist(rho.1,xlim=c(0.3,1.3),ylim=c(0,600),breaks=seq(0.3,1.3,0.025),
     main="Posterior: Difficulty Deviation, c=1",
     xlab="rho.1",
     ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="logistic-gamma_0-fit.pdf",
    onefile=FALSE)
mean.gamma.0 <- rep(NA,J)
for (j in 1:J) {
  mean.gamma.0[j] <- mean(gamma.0[,j])
}
plot(sim.gamma.0,mean.gamma.0,
     xlim=c(-1,6), ylim=c(-1,6), pch=20,
     main="Estimated vs. Simulated gamma.0",
     xlab="simulated gamma.0",
     ylab="mean estimate gamma.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J) {
  lines(sim.gamma.0[j] * c(1,1),quantile(gamma.0[,j],c(0.025,0.975)))
}
abline(coef=c(0,1), col="lightgrey")
dev.off()


pdf(file="logistic-gamma_1-fit.pdf",
    onefile=FALSE)
mean.gamma.1 <- rep(NA,J)
for (j in 1:J) {
  mean.gamma.1[j] <- mean(gamma.1[,j])
}
plot(sim.gamma.1,mean.gamma.1,
     xlim=c(-1,6), ylim=c(-1,6), pch=20,
     main="Estimated vs. Simulated gamma.1",
     xlab="simulated gamma.1",
     ylab="mean estimate gamma.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (j in 1:J) {
  lines(sim.gamma.1[j] * c(1,1),quantile(gamma.1[,j],c(0.025,0.975)))
}
abline(coef=c(0,1), col="lightgrey")
dev.off()


pdf(file="logistic-delta_0-fit.pdf",
    onefile=FALSE)
mean.delta <- rep(NA,I)
for (i in 1:I)
  mean.delta[i] <- mean(delta[,i])
plot(sim.delta[sim.c==0],mean.delta[sim.c==0],
     xlim=c(-3,3), ylim=c(-3,3),
     pch=20,
     main="Estimated vs. Simulated delta, c=0",
     xlab="simulated delta",
     ylab="mean estimate delta",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
abline(coef=c(0,1), col="lightgrey")
dev.off()

pdf(file="logistic-delta_1-fit.pdf",
    onefile=FALSE)
plot(sim.delta[sim.c==1],mean.delta[sim.c==1],
     xlim=c(-3,3), ylim=c(-3,3),
     pch=20,
     main="Estimated vs. Simulated delta, c=1",
     xlab="simulated delta",
     ylab="mean estimate delta",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
abline(coef=c(0,1), col="lightgrey")
dev.off()
