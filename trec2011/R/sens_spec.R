library("ggplot2")
theta0_hat = rep(0,J);
theta1_hat = rep(0,J);
for (j in 1:J) {
  theta0_hat[j] = mean(theta0[,j]);
  theta1_hat[j] = mean(theta1[,j]);
}
p <- qplot(theta0_hat,theta1_hat,geom=c("point"),
           xlab="specificity", ylab="sensitivity", main="Sensitivity vs. Specificity (Estimated)") + geom_abline(intercept = 1, slope = -1,colour="red");
p;

