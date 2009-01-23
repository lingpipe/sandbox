num.states = (attributes(corr)$dim)[[1]]


ll <- rep(NA,num.states)
for (s in 1:num.states)
  ll[s] = sum(log(corr[s,]))


