pdf(file="f-measure-contour.pdf",height=4,width=4)
contour(x,y,z,levels=seq(0,1,by=0.1), frame=FALSE, xlab="recall", ylab="precision",cex.lab=1.25)
dev.off()
