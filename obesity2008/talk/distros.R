pdf(file="prior.pdf")
hist(rbeta(5000000,20,100),axes=FALSE,xlim=c(0,1),breaks=seq(0,1,.001),
     main="Prior", xlab="theta", ylab="p( theta )",
     cex.main=2.5, cex.lab=1.5, cex.axis=2)
axis(side=1,labels=c("0",".5","1"),at=c(0,0.5,1),cex.lab=1.5,cex.axis=2)
dev.off()

pdf(file="yes.pdf")
hist(rbeta(5000000,2,40),axes=FALSE,xlim=c(0,1),breaks=seq(0,1,.001),
     main="NO Posteror", xlab="theta", ylab="p( theta | text )",
     cex.main=2.5, cex.lab=1.5, cex.axis=2)
axis(side=1,labels=c("0",".5","1"),at=c(0,0.5,1),cex.lab=1.5,cex.axis=2)
dev.off()

pdf(file="no.pdf")
hist(rbeta(5000000,20,1),axes=FALSE,xlim=c(0,1),breaks=seq(0,1,.001),
     main="YES Posteror", xlab="theta", ylab="p( theta | text )",
     cex.main=2.5, cex.lab=1.5, cex.axis=2)
axis(side=1,labels=c("0",".5","1"),at=c(0,0.5,1),cex.lab=1.5,cex.axis=2)
dev.off()

pdf(file="questionable.pdf")
hist(rbeta(5000000,10,10),axes=FALSE,xlim=c(0,1),breaks=seq(0,1,.001),
     main="QUESTIONABLE Posterior", xlab="theta", ylab="p( theta | text )",
     cex.main=2.5, cex.lab=1.5, cex.axis=2)
axis(side=1,labels=c("0",".5","1"),at=c(0,0.5,1),cex.lab=1.5,cex.axis=2)
dev.off()

pdf(file="unknown.pdf")
hist(rbeta(5000000,20,100),axes=FALSE,xlim=c(0,1),breaks=seq(0,1,.001),
     main="UNKNOWN Posterior", xlab="theta", ylab="p( theta | text )",
     cex.main=2.5, cex.lab=1.5, cex.axis=2)
axis(side=1,labels=c("0",".5","1"),at=c(0,0.5,1),cex.lab=1.5,cex.axis=2)
dev.off()


