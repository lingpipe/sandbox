colors = c("red","orange","yellow","green","blue","purple")

pdf(file="dentistry-theta_0-hist.pdf",onefile=FALSE)
hist(theta.0[,1],col=colors[1],border=colors[1],xlim=c(0.3,1.0),ylim=c(0,200),
     main="Annotator Specificities", xlab="theta.0", ylab=NULL, 
     freq=FALSE,breaks=70,
     axes=FALSE,
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
axis(side=1)
legend(0.30,200,1:5,col=colors,lty=1,title="Annotator Key",
       cex=1.5,pt.lwd=10,lwd=20)
for (j in 2:J) {
  hist(theta.0[,j],col=colors[j],border=colors[j],add=TRUE,breaks=50,
       axes=FALSE)
}
dev.off()


pdf(file="dentistry-theta_1-hist.pdf",onefile=FALSE)
hist(theta.1[,1],col=colors[1],border=colors[1],xlim=c(0.3,1.0),ylim=c(0,200),
     main="Annotator Sensitivities", xlab="theta.1", ylab=NULL, freq=FALSE,breaks=70,
     axes=FALSE,
     cex.main=1.75, cex.axis=1.5, cex.sub=1.5, cex.lab=1.5)
axis(side=1)
for (j in 2:J) {
  hist(theta.1[,j],col=colors[j],border=colors[j],add=TRUE,breaks=50,
       cex=2,cex.axis=2,cex.lab=2,cex.main=2)
}
dev.off()

