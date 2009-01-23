spec <- rep(NA,J)
for (j in 1:J)
  spec[j] <- sum(jj == j & c.gold[ii] == 0 & xx == 0) / sum(jj==j & c.gold[ii] == 0)

sens <- rep(NA,J)
for (j in 1:J)
  sens[j] <- sum(jj == j & c.gold[ii] == 1 & xx == 1) / sum(jj==j & c.gold[ii] == 1)


spec.hat <- rep(NA,J)
for (j in 1:J)
  spec.hat[j] <- median(theta.0[,j])

sens.hat <- rep(NA,J)
for (j in 1:J)
  sens.hat[j] <- median(theta.1[,j])

