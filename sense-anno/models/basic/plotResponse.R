library("ggplot2");

meanTheta <- array(0,c(J,K,K));
for (j in 1:J) {
  for (k in 1:K) {
    for (k2 in 1:K) {
      meanTheta[j,k,k2] <- mean(theta[,j,k,k2]);
    }
  }
}
meanTheta1 <- data.frame(meanTheta[1,1:K,1:K]);
df <- melt(meanTheta);

logit = function(x) { log(x/(1-x)); }


#pdf(file="response1.pdf");

p <- ggplot(df,aes(x=X3,y=X2))
p <- p + geom_tile(aes(fill=logit(value)),colour="white");
p <- p + scale_fill_gradient(low="black",high="white");
p <- p + facet_wrap(~ X1,nrow=1);
# p <- p + coord_equal();
p <- p + opts(aspect.ratio=1, title="thetaHat[j]");
  
#dev.off();


