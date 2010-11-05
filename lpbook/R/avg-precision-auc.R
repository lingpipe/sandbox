plot.pr <- function(x,y,title,pch) {
#  lines(c(0,1),c(0,1),type="l",lty=2)
}

#pdf(file="avg-precision-auc.pdf",width=6,height=5)

x = c(0, 0.2, 0.4, 0.6, 0.8,  1.0)
y = c(0.6, 0.6, 0.6, 0.6, 0.44, 0.0)

plot(x,y,
     type="p", pch=20,
     xlim=c(0,1),ylim=c(0,1),
     xlab="recall",ylab="precision", 
     cex.axis=1.15, cex.lab=1.25, cex.main=1.15,
     frame=FALSE)
for (i in 2:(length(x))) {
    lines(c(x[i-1],x[i]),c(y[i],y[i]),type="l",lty=1)
}
for (i in 3:(length(x))) {
    lines(c(x[i-1],x[i-1]),c(0,y[i-1]),type="l",lty=2)
}

#dev.off()