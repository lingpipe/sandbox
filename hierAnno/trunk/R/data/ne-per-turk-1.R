rte.tsv <- scan("data/mturk-ne/per.anno.tsv")
K.0 <- length(rte.tsv) / 3
mat.rte <- matrix(rte.tsv,nrow=K.0,ncol=3,byrow=TRUE)
ii.0 <- mat.rte[,1]
jj.0 <- mat.rte[,2]
xx.0 <- mat.rte[,3]
I.0 <- max(ii.0)
J.0 <- max(jj.0)

c.gold.0 <- scan("data/mturk-ne/per.gold.tsv")


I <- I.0
J <- J.0
K <- K.0
ii <- ii.0
jj <- jj.0
xx <- xx.0
c.gold <- c.gold.0


keep <- rep(FALSE,I.0)
for (k in 1:K.0) {
  if (xx.0[k] == 1) {
    keep[ii.0[k]] = TRUE
  }
}
for (i in 1:I.0) {
  if (c.gold[i] == 1)
    keep[i] = TRUE
}


keep.map <- rep(-1,I.0)
keep.map.inv <- rep(NA,sum(keep))
idx <- 0
for (i in 1:I.0) {
  if (keep[i]) {
    idx <- idx + 1
    keep.map[i] <- idx
    keep.map.inv[idx] <- i
  }
}



I <- sum(keep)
J <- J.0
K <- 0
for (k in 1:K.0) {
  if (keep[ii.0[k]]) {
    K <- K + 1
  }
}

ii <- rep(NA,K)
jj <- rep(NA,K)
xx <- rep(NA,K)
idx <- 0
for (k in 1:K.0) {
  if (keep[ii.0[k]]) {
    idx <- idx + 1
    ii[idx] <- keep.map[ii.0[k]]
    jj[idx] <- jj.0[k]
    xx[idx] <- xx.0[k]
  }
}

c.gold <- rep(NA,I)
for (i in 1:I.0) {
  if (keep[i]) {
    c.gold[keep.map[i]] <- c.gold.0[i]
  }
}