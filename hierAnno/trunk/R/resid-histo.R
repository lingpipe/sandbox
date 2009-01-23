abc <- rep(NA,I)
for (i in 1:I)
  abc[i] <- mean(abs(c[,i] - sim.c[i]))
histogram(abc[abc>0.01],n=60,type="count",endpoints=c(0.01,1),xlab="absolute error",main="Absolute Errors > 0.01")
