library("concord")
kappas.c <- matrix(data=NA,J,J)
kappas.sc <- matrix(data=NA,J,J)
kappas.bbc <- matrix(data=NA,J,J)
for (j1 in 1:(J-1)) {
  for (j2 in (j1+1):J) {
    m12 <- cbind(x[,j1],x[,j2])
    ks <- cohen.kappa(m12)
    kappas.c[j1,j2] = ks$kappa.c
    kappas.sc[j1,j2] = ks$kappa.sc
    kappas.bbc[j1,j2] = ks$kappa.bbc
  }
}
print("Cohen's kappa")
print(kappas.c,digit=2)
print("Siegel and Castellan's kappa")
print(kappas.sc,digits=2)
print("Byrt, Bishop and Carlin's kappa")
print(kappas.bbc,digits=2)

nsamps <- nrow(c)
kappas.imputed.c <- matrix(data=NA,ncol=J,nrow=nsamps)
kappas.imputed.sc <- matrix(data=NA,ncol=J,nrow=nsamps)
kappas.imputed.bbc <- matrix(data=NA,ncol=J,nrow=nsamps)
for (k in 1:nsamps) {
  xj <- x[,j]
  for (j in 1:J) {
    m <- cbind(xj,c[k,])
    kappas.imputed.c[k,j] <- cohen.kappa(m)$kappa.c
    kappas.imputed.sc[k,j] <- cohen.kappa(m)$kappa.sc
    kappas.imputed.bbc[k,j] <- cohen.kappa(m)$kappa.bbc
  }
}
for (j in 1:J) {
  print("annotator")
  print(j)
  print("true accuracy")
  print(sim.a[j])
  print("imputed accuracy")
  print(mean(a[j]))
  print("Cohens")
  print("kappa mean")
  print(mean(kappas.imputed.c[,j]))
  print("kappa quantiles")
  print(quantile(kappas.imputed.c[,j],c(0.025,0.5,.975)))
  print("Siegel and Castellan")
  print("kappa mean")
  print(mean(kappas.imputed.sc[,j]))
  print("kappa quantiles")
  print(quantile(kappas.imputed.sc[,j],c(0.025,0.5,.975)))
  print("Byrt, Bishop, and Carlin")
  print("kappa mean")
  print(mean(kappas.imputed.bbc[,j]))
  print("kappa quantiles")
  print(quantile(kappas.imputed.bbc[,j],c(0.025,0.5,.975)))
}