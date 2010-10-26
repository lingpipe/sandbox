pdf(file="pr-curves-2.pdf",width=8,height=5)
par(mfcol=c(1,2))

x2=c(0.0, 0.2, 0.2,  0.4, 0.6, 0.6, 0.6,  0.6,  0.8,  0.8, 1.0)
y2=c(1.0, 0.5, 0.33, 0.5, 0.6, 0.5, 0.43, 0.38, 0.44, 0.4, 0.0)
plot(x2,y2,
     type="o", pch=20,
     xlim=c(0,1),ylim=c(0,1),
     xlab="recall",ylab="precision",
     cex.axis=1.15, cex.lab=1.25, cex.main=1.25,
     frame=FALSE)

x1 = c(0.0, 0.2, 0.4, 0.6, 0.8,  1.0)
y1 = c(1.0, 0.6, 0.6, 0.6, 0.44, 0.0)
lines(x1,y1,
     type="o", pch=2, lty=2)

dev.off()