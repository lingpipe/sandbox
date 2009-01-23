# works for dense
c.maj <- rep(NA,I)
for (i in 1:I) {
  num.ones <- sum(x[i,])
  mid <- length(x[i,]) / 2
  if (num.ones == mid)
    c.maj[i] <- rbinom(1,1,0.5)
  else if (num.ones > mid)
    c.maj[i] <- 1
  else
    c.maj[i] <- 0
}

