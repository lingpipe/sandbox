num.samples <- length(pi)

ct.cell <- rep("",I)
for (i in 1:I) {
  ct.cell[i] <- paste(x[i,],collapse="")
}
ct.cell.factor <- factor(ct.cell)
ct.cell.levels <- levels(ct.cell.factor)
sum.empirical <- tapply(rep(1,times=I),ct.cell.factor,sum)

num.levels <- length(ct.cell.levels)

sum.gen <- matrix(0,num.samples,num.levels)
for (samp in 1:num.samples) {
  ct.cell.gen <- rep("",I)
  for (i in 1:I) {
    ct.cell.gen[i] <- paste(x.gen[samp,i,],collapse="")
  }
  ct.cell.factor.gen <- factor(ct.cell.gen)
  sum.empirical.gen <- tapply(rep(1,times=I),ct.cell.factor.gen,sum)
  for (n in 1:num.levels) {
    level <- ct.cell.levels[n]
    for (i in 1:I) {
      if (ct.cell.gen[i] == level)
        sum.gen[samp,n] <- sum.gen[samp,n] + 1
    }
  }
}

for (n in 1:num.levels) {
  print(paste(c(ct.cell.levels[n],"  ",
                format(quantile(sum.gen[,n],0.025)[[1]],digits=2)," ",
                format(quantile(sum.gen[,n],0.5)[[1]],digits=2),"  ",
                format(quantile(sum.gen[,n],0.975)[[1]],digits=2)),
              collapse=""),
        quote=FALSE)
}

es.mean <- rep(NA,32)
for (n in 1:num.levels) {
  es.mean[n] <- mean(sum.gen[,n])[[1]]
}

print(chisq.test(sum.empirical,y=NULL,correct=FALSE,p=es.mean,rescale.p=TRUE))