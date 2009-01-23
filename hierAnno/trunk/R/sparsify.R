# Bob Carpenter
# Alias-i
# 8 September 2008

# Code used in paper submitted to NYAS ML Symposium
# http://lingpipe.files.wordpress.com/2008/09/hierarchical-data-annotation-nyas-08.pdf

K <- 0
for (i in 1:I) {
  for (j in 1:J) {
     if (is.finite(x[i,j])) {
       K <- K + 1
     }
  }
}
ii <- rep(NA,K)
jj <- rep(NA,K)
xx <- rep(NA,K)
k <- 0
for (i in 1:I) {
  for (j in 1:J) {
     if (is.finite(x[i,j])) {
       k <- k + 1
       ii[k] = i
       jj[k] = j
       xx[k] = x[i,j]
     }
  }
}


