pdf(file="beta-binomial-item-scatter-spec.pdf",
    onefile=FALSE)
plot(acc.0,scale.0,xlim=c(0.60,0.90),ylim=c(0,100),pch=20,
     main="Posterior: Specificity Mean & Scale",
     xlab="alpha.0 + beta.0",
     ylab="alpha.0 / (alpha.0 + beta.0)",
     cex=1.5,cex.axis=1.5,cex.lab=1.5,cex.main=1.5)
h = sim.alpha.0 + sim.beta.0
abline(h=h)
v = sim.alpha.0/(sim.alpha.0 + sim.beta.0)
abline(v=v)
dev.off()


pdf(file="beta-binomial-item-scatter-sens.pdf",
    onefile=FALSE)
plot(acc.1,scale.1,xlim=c(0.60,0.90),ylim=c(0,100),pch=20,
     main="Posterior: Sensitivity Mean & Scale",
     xlab="alpha.1 + beta.1",
     ylab="alpha.1 / (alpha.1 + beta.1)",
     cex=1.5,cex.axis=1.5,cex.lab=1.5,cex.main=1.5)
h = sim.alpha.1 + sim.beta.1
abline(h=h)
v = sim.alpha.1/(sim.alpha.1 + sim.beta.1)
abline(v=v)
dev.off()


