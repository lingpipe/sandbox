library("ggplot2");

z1_post = rep(0.0,I1)
for (i in 1:I1) {
    z1_post[i] <- mean(z1[,i]);
}


pp <- qplot(z1_voted,z1_post,geom=c("point","smooth"),alpha=I(1/20),
      xlab="voted relevance", ylab="estimated relevance", main="Estimated vs. Voted Relevance");

# pdf(file="../paper/img/vote_vs_estimate_2.pdf",width=4,height=4);
# pp;
# dev.off();


# histogram(z1_sum,
#           main="number of workers per item")
