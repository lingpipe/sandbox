c.gold.simple <- c.gold
c.gold.simple[c.gold.simple == -1] = 0

resids <- rep(NA,I)
for (i in 1:I) {
  resids[i] <- (max(0,c.gold.simple[i]) - mean(c[,i]))
  if (abs(resids[i]) > 0.5)
    print(c(keep.map.inv[i],resids[i]),digits=6)
}

pdf(file="ne-cat-resids-model.pdf",
    onefile=FALSE)
hist(resids,xlim=c(-1,1),col="blue",
     main="Residual Category Error (Model)",
     xlab="residual error", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

voted.resids <- rep(NA,I)
for (i in 1:I)
  voted.resids[i] <- (c.gold.simple[i] - mean(xx[ii == i & is.finite(xx)]))

pdf(file="ne-cat-resids-voted.pdf",
    onefile=FALSE)
hist(voted.resids,xlim=c(-1,1),col="blue",
     main="Residual Category Error (Voted)",
     xlab="residual error", ylab="frequency",
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
dev.off()

# RESIDS = GOLD - ESTIMATE
print(c("|resid| >= 0.5",sum(abs(resids) >= 0.5)))
print(c("FP: resid <= -0.5",sum(resids <= -0.5)))
print(c("FN: resid >= 0.5",sum(resids >= 0.5)))

gt5 <- sum(abs(voted.resids) > 0.5)
print(c("|majority.resid| > 0.5",gt5))
eq5 <- sum(abs(voted.resids) == 0.5)
print(c("|majority.resid| == 0.5",eq5))
print(c("majority exp errors=",gt5 + eq5/2))
