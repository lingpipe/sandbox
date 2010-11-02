png(file="log-loss.png",width=400,height=400)
p <- 0.7;
f <- function(q) {
    - (p * log(q) + (1-p) * log(1-q));
}
curve(f, xlim=c(0,1), ylim=c(0,4),
      xlab="yHat", ylab="LogLoss(y, yHat)",
      main="Log Loss  ( y=0.7 )",
      lwd=2, cex.lab=1.5, cex.axis=1.5,cex.main=1.5,
      axes=FALSE, frame=FALSE)

axis(1,at=c(0,0.5,0.7,1))
axis(2,at=c(0,f(0.7),2,4),labels=c("0", ".61", "2", "4"))
abline(h=f(0.7),col="blue",lty=3)
abline(v=0.7,col="red",lty=2) 
dev.off()
