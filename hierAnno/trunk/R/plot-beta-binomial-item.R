pdf(file="beta-binomial-item-posterior-pi.pdf")
hist(pi,
     breaks=seq(0.12:0.25,by=0.005),
     xlim=c(0.12,0.25), ylim=c(0,200),
     main="Posterior: pi",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()


pdf(file="beta-binomial-item-sim-theta-c0.pdf", onefile=FALSE)
hist(sim.theta[sim.c==0],
     xlim=c(0.4,1.0), ylim=c(0,80), breaks=seq(0.4,1.0,0.01),
     main="Simulated theta, c=0",
     xlab="sim.theta", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-item-sim-theta-c1.pdf", onefile=FALSE)
hist(sim.theta[sim.c==1],
     xlim=c(0.4,1.0), ylim=c(0,80),  breaks=seq(0.4,1.0,0.01),
     main="Simulated theta, c=1",
     xlab="sim.theta", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()


pdf(file="beta-binomial-item-scatter-spec.pdf",
    onefile=FALSE)
plot(acc.0,scale.0,xlim=c(0.60,0.90),ylim=c(0,250),pch=20,
     main="Posterior: Specificity Mean & Scale",
     xlab="alpha.0 + beta.0",
     ylab="alpha.0 / (alpha.0 + beta.0)",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.0 + sim.beta.0
abline(h=h)
v = sim.alpha.0/(sim.alpha.0 + sim.beta.0)
abline(v=v)
dev.off()

pdf(file="beta-binomial-item-scatter-sens.pdf",
    onefile=FALSE)
plot(acc.1,scale.1,xlim=c(0.60,0.90),ylim=c(0,250),pch=20,
     main="Posterior: Specificity Mean & Scale",
     xlab="alpha.1 + beta.1",
     ylab="alpha.1 / (alpha.1 + beta.1)",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.1 + sim.beta.1
abline(h=h)
v = sim.alpha.1/(sim.alpha.1 + sim.beta.1)
abline(v=v)
dev.off()

num.chains <- 3
num.samples <- length(scale.1)/3
pdf(file="beta-binomial-item-traceplot-scale1.pdf",
    onefile=FALSE)
plot(scale.1[1:num.samples], type="l",  ylim=c(0,500),
     col="RED",
     xlab="Gibbs Sample",ylab="alpha.1 + beta.1",main="Gibbs Samples: alpha.1 + beta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
lines(scale.1[(num.samples+1):(2*num.samples)],col="YELLOW")
lines(scale.1[(2*num.samples+1):(3*num.samples)],col="BLUE")
abline(h=median(scale.1))
abline(h=quantile(scale.1,0.025))
abline(h=quantile(scale.1,0.975))
dev.off()



pdf(file="beta-binomial-item-theta-0-fit.pdf",
    onefile=FALSE)
mean.theta <- rep(NA,I)
for (i in 1:I)
  mean.theta[i] <- mean(theta[,i])
plot(sim.theta[sim.c==0],mean.theta[sim.c==0],
     xlim=c(0.5,1.0), ylim=c(0.5,1.0), pch=20,
     main="Estimated vs. Simulated theta, c=0",
     xlab="simulated theta",
     ylab="mean estimate theta",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (i in 1:I) {
  if (sim.c[i] == 0) {
#    lines(sim.theta[i] * c(1,1),quantile(theta[,i],c(0.025,0.975)))
  }
}
abline(coef=c(0,1), col="lightgrey")
dev.off()

pdf(file="beta-binomial-item-theta-1-fit.pdf",
    onefile=FALSE)
mean.theta <- rep(NA,I)
for (i in 1:I)
  mean.theta[i] <- mean(theta[,i])
plot(sim.theta[sim.c==1],mean.theta[sim.c==1],
     xlim=c(0.5,1.0), ylim=c(0.5,1.0), pch=20,
     main="Estimated vs. Simulated theta, c=1",
     xlab="simulated theta",
     ylab="mean estimate theta",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
for (i in 1:I) {
  if (sim.c[i] == 1) {
#    lines(sim.theta[i] * c(1,1),quantile(theta[,i],c(0.025,0.975)))
  }
}
abline(coef=c(0,1), col="lightgrey")
dev.off()


pdf(file="beta-binomial-item-scatter-spec.pdf",
    onefile=FALSE)
plot(acc.0,scale.0,xlim=c(0.60,0.90),ylim=c(0,300),pch=20,
     main="Posterior: Specificity Mean & Scale",
     xlab="alpha.0 / (alpha.0 + beta.0)",
     ylab="alpha.0 + beta.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.0 + sim.beta.0
abline(h=h)
v = sim.alpha.0/(sim.alpha.0 + sim.beta.0)
abline(v=v)
dev.off()

pdf(file="beta-binomial-item-scatter-sens.pdf",
    onefile=FALSE)
plot(acc.1,scale.1,xlim=c(0.60,0.90),ylim=c(0,300),pch=20,
     main="Posterior: Sensitivity Mean & Scale",
     xlab="alpha.1 / (alpha.1 + beta.1)",
     ylab="alpha.1 + beta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
h = sim.alpha.1 + sim.beta.1
abline(h=h)
v = sim.alpha.1/(sim.alpha.1 + sim.beta.1)
abline(v=v)
dev.off()



pdf(file="beta-binomial-item-post-theta_1.pdf", onefile=FALSE)
hist(theta[,1],
     breaks=seq(0.3,1.0,0.02),
     xlim=c(0.3,1.0), ylim=c(0,250),
     pch=20,
     main="Posterior: theta[1]  (c  = 1)",
     xlab="theta[1]", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-item-post-theta_2.pdf", onefile=FALSE)
hist(theta[,2],
     breaks=seq(0.3,1.0,0.02),
     xlim=c(0.3,1.0), ylim=c(0,250),
     pch=20,
     main="Posterior: theta[2]  (c = 0)",
     xlab="theta[2]", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="beta-binomial-item-cat-residual.pdf",
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

