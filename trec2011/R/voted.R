z1_post = rep(0.0,I1)
for (i in 1:I1) {
    z1_post[i] <- mean(z1[,i]);
}

library("ggplot2");
qplot(z1_voted,z1_post,geom=c("point","smooth"),alpha=I(1/5),   main="voted vs. modeled");

# histogram(z1_sum,
#           main="number of workers per item")
