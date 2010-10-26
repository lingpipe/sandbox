# Ugly cut-and-paste from pr-curve.R

plot.ss <- function(x,y,title) {
  plot(x,y,
       type="o", pch=20,
       xlim=c(0,1),ylim=c(0,1),
       xlab="1 - specificity",ylab="sensitivity", main=title,
       cex.axis=1.15, cex.lab=1.25, cex.main=1.15,
       frame=FALSE)
}
pdf(file="roc-curve.pdf",width=9,height=5)
par(mfcol=c(1,2))

x2=c(0, .17, .17, .33, .33, .33, .5, .67, .83, .83, 1, 1)
y2=c(0, 0, .2, .2, .4, .6, .6, .6, .6, .8, .8, 1)

plot.ss(x2,y2,"Uninterpolated")
lines(c(0,1),c(0,1),type="l",lty=2)

x1 = c(0,0.17,0.33,.5,0.67,0.83,1)
y1 = c(0,.2,.6,.6,.6,.8,1)
plot.ss(x1,y1,"Interpolated")
points(x2,y2,pch=5)
lines(c(0,1),c(0,1),type="l",lty=2)

dev.off()
