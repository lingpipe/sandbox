num.samples <- length(pi)

ct.cell <- rep("a",times=J)
for (i in 1:I) {
  ct.cell[i] = paste(x[i,],collapse="")
}
ct.cell.factor <- factor(ct.cell)
ct.cell.levels <- levels(ct.cell.factor)
sum.empirical <- tapply(rep(1,times=I),ct.cell.factor,sum)


print("cell     empirical.count       expected.count",quote=FALSE)


es.mean <- rep(NA,length(ct.cell.levels))
for (level in 1:length(ct.cell.levels)) {
  s <- ct.cell.levels[level]
  split <- strsplit(s,"")
  es <- rep(NA,num.samples)
  for (samp in 1:num.samples) {
    expected.0 <- (1.0 - pi[samp]) 
    expected.1 <- pi[samp] 
    for (j in 1:J) {
      expected.0 <- expected.0 * ifelse("0" == split[[1]][j],
                                        theta.0[samp,j],
                                        (1 - theta.0[samp,j]))
      expected.1 <- expected.1 * ifelse("1" == split[[1]][j],
                                        theta.1[samp,j],
                                        (1 - theta.1[samp,j]))
    }
    es[samp] <- I * (expected.0 + expected.1)
  }
  es.mean[level] <- mean(es)
  print(paste(c(split[[1]],"  ",
                sum.empirical[[s]]," (",
                format(quantile(es,0.025)[[1]],digits=2),", ",
                format(quantile(es,0.5)[[1]],digits=2),",  ",
                format(quantile(es,0.975)[[1]],digits=2), ")"),
              collapse=""),
        quote=FALSE)
}

print(chisq.test(sum.empirical,y=NULL,correct=FALSE,p=es.mean,rescale.p=TRUE))
