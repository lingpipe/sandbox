
pdf(file="binomial-posterior-pi.pdf")
hist(pi,
     breaks=seq(0.15:0.25,by=0.002),
     xlim=c(0.15,0.25), ylim=c(0,350),
     main="Posterior: pi",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="binomial-posterior-theta0.pdf")
hist(theta.0,
     breaks=seq(0.78:0.88,by=0.002),
     xlim=c(0.78,0.88), ylim=c(0,350),
     main="Posterior: theta.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

pdf(file="binomial-posterior-theta1.pdf")
hist(theta.1,
     breaks=seq(0.625:0.725,by=0.002),
     xlim=c(0.625,0.725), ylim=c(0,350),
     main="Posterior: theta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

num.samples <- length(theta.1)/3


pdf(file="binomial-traceplot-pi.pdf",
    onefile=FALSE)
plot(pi[1:num.samples], type="l",
     ylim=c(0.15,0.25),
     col="RED",
     xlab="Gibbs Sample",ylab="pi",main="Gibbs Samples: pi",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
lines(pi[(num.samples+1):(2*num.samples)],col="YELLOW")
lines(pi[(2*num.samples+1):(3*num.samples)],col="BLUE")
abline(h=median(pi))
abline(h=quantile(pi,0.025))
abline(h=quantile(pi,0.975))
dev.off()

pdf(file="binomial-traceplot-theta0.pdf",
    onefile=FALSE)
plot(theta.0[1:num.samples], type="l",  ylim=c(0.78,0.88),
     col="RED",
     xlab="Gibbs Sample",ylab="theta.0",main="Gibbs Samples: theta.0",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
lines(theta.0[(num.samples+1):(2*num.samples)],col="YELLOW")
lines(theta.0[(2*num.samples+1):(3*num.samples)],col="BLUE")
abline(h=median(theta.0))
abline(h=quantile(theta.0,0.025))
abline(h=quantile(theta.0,0.975))
dev.off()

pdf(file="binomial-traceplot-theta1.pdf",
    onefile=FALSE)
plot(theta.1[1:num.samples], type="l",  ylim=c(0.625,0.725),
     col="RED",
     xlab="Gibbs Sample",ylab="theta.1",main="Gibbs Samples: theta.1",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
lines(theta.1[(num.samples+1):(2*num.samples)],col="YELLOW")
lines(theta.1[(2*num.samples+1):(3*num.samples)],col="BLUE")
abline(h=median(theta.1))
abline(h=quantile(theta.1,0.025))
abline(h=quantile(theta.1,0.975))
dev.off()

pdf(file="binomial-cat-residual.pdf",
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

