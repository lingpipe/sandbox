# filters out annotators with sensitivity or specificites
# estimated below 0.5 -- better way to do this would be within
# a given distance of the chance performance line

filter <- rep(TRUE,J)
for (j in 1:J)
  filter[j] <- (median(theta.0[,j]) < 0.5) | (median(theta.1[,j]) < 0.5)

for (k in 1:K)
  if (filter[jj[k]])
    xx[k] <- NA


