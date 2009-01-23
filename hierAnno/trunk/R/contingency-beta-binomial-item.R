library("VGAM")  # for beta-binomial

num.samples <- length(pi)

ct.cell <- rep("a",times=J)
for (i in 1:I) {
  ct.cell[i] = paste(x[i,],collapse="")
}
ct.cell.factor <- factor(ct.cell)
ct.cell.levels <- levels(ct.cell.factor)
sum.empirical <- tapply(rep(1,times=I),ct.cell.factor,sum)


print("cell     empirical.count       expected.count",quote=FALSE)

expected.margs <- matrix(NA,num.samples,J+1)
for (samp in 1:num.samples) {
  for (n in 0:J) {
    expected.margs[samp,n+1] <- I * (pi[samp] * dbetabin.ab(n,J,alpha.1[samp],beta.1[samp])  + (1 - pi[samp]) * dbetabin.ab(n,J,beta.0[samp],alpha.0[samp])) / choose(J,n)
  }
}

for (n in 0:J) {
  print(c(n,choose(J,n),quantile(expected.margs[,n+1],c(0.025,0.25,0.5,0.75,0.975))),digits=3)
}

es.mean <- rep(NA,length(ct.cell.levels))
for (level in 1:length(ct.cell.levels)) {
  s <- ct.cell.levels[level]
  split <- strsplit(s,"")
  n <- 0
  for (j in 1:J) {
    if("1" == split[[1]][j]) {
      n <- n + 1
    }
  }
  es.mean[level] <- mean(expected.margs[,n+1])
}

print(chisq.test(sum.empirical,y=NULL,correct=FALSE,p=es.mean,rescale.p=TRUE))

for (n in 0:J)
  print(c(n,quantile(choose(J,n) * expected.margs[,n+1],c(0.025,0.5,0.975))),digits=3)