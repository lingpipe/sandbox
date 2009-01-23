pdf(file="beta-binomial-item-post-theta0.pdf", onefile=FALSE)
hist(theta.0,breaks=20,
     xlim=c(0.4,1.0), ylim=c(0,2500),
     pch=20,
     main="Posterior: theta.0[2]",
     xlab="theta.0", ylab="frequency",
     cex=1.5, cex.axis=1.5, cex.lab=1.5, cex.main=1.5)
dev.off()

pdf(file="beta-binomial-item-post-theta1.pdf", onefile=FALSE)
hist(theta.1,breaks=20,
     xlim=c(0.4,1.0), ylim=c(0,2500),
     pch=20,
     main="Posterior: theta.1[2]",
     xlab="theta.0", ylab="frequency",
     cex=1.5, cex.axis=1.5, cex.lab=1.5, cex.main=1.5)
dev.off()
