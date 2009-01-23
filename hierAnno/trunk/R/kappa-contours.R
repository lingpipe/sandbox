x <- seq(0,1,0.01)

acc <- matrix(nrow=length(x), ncol=length(x))

rownames(acc) <- x;
colnames(acc) <- x;

for (i in 1:length(x))
  for (k in 1:length(x))
        acc[i,k] <- x[i]*x[k] + (1-x[i])*(1-x[k]);


for (prevalence in c(0.5,0.7,0.9)) {
  expected <- prevalence * prevalence + (1 - prevalence) * (1 - prevalence)
  kappa <- (acc - expected) / (1 - expected)

  filename <- paste(c("kappa-",prevalence,".pdf"),
                    collapse="")
  pdf(file=filename,onefile=FALSE)

  contour(kappa,
          levels=seq(-1,1,0.1),
          xlim=c(0.6,1),
          ylim=c(0.6,1),
          xlab="Annotator 1 Accuracy",
          ylab="Annotator 2 Accuracy")

#  abline(coef=c(0,1),col="RED")
  title(paste(c("S&C's Kappa (prevalence=",prevalence,")"),
              collapse=""))

  dev.off()
}