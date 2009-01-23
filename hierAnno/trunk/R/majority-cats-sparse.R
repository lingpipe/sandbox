# for sparse
c.ones <- rep(0,I)
c.zeros <- rep(0,I)
for (k in 1:K) {
  if (xx[k] == 1) {
    c.ones[ii[k]] <- c.ones[ii[k]] + 1
  } else {
    c.zeros[ii[k]] <- c.zeros[ii[k]] + 1
  }
}
c.maj <- rep(NA,I)
for (i in 1:I) {
   if (c.ones[i] > c.zeros[i]) {
     c.maj[i] <- 1
   } else if (c.ones[i] < c.zeros[i]) {
     c.maj[i] <- 0
   } else {
     c.maj[i] <- rbinom(1,1,0.5)
   }
}