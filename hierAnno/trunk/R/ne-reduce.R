# reduce to L samples

L <- 10

I.0 <- I
J.0 <- J
K.0 <- K
ii.0 <- ii
jj.0 <- jj
xx.0 <- xx

# reverse everything
reverse <- TRUE
if (reverse) {
  for (k in 1:(K/2)) {
    tmp.ii.0 <- ii.0[k]
    ii.0[k] <- ii.0[K-k+1]
    ii.0[K-k+1] <- tmp.ii.0

    tmp.jj.0 <- jj.0[k]
    jj.0[k] <- jj.0[K-k+1]
    jj.0[K-k+1] <- tmp.jj.0

    tmp.xx.0 <- xx.0[k]
    xx.0[k] <- xx.0[K-k+1]
    xx.0[K-k+1] <- tmp.xx.0
  }
}



itemCount <- rep(0,I.0)
keep.map <- rep(-1,K.0)
jj.map <- rep(-1,J.0)
J <- 0
K <- 0
for (k in 1:K.0) {
  if (itemCount[ii.0[k]] < L) {
    itemCount[ii.0[k]] <- itemCount[ii.0[k]] + 1
    K <- K + 1
    keep.map[k] <- K
    if (jj.map[jj.0[k]] == -1) {
      J <- J + 1
      jj.map[jj.0[k]] <- J
    }
  }
}


ii <- rep(-1,K)
jj <- rep(-1,K)
xx <- rep(-1,K)
for (k in 1:K.0) {
  idx <- keep.map[k]
  if (idx != -1) {
    ii[idx] <- ii.0[k]
    jj[idx] <- jj.map[jj.0[k]]
    xx[idx] <- xx.0[k]
  }
}

print(c("L",L,"I",I,"J",J,"K",K))