confusion.matrix <-
    c(rep(c(0,0,0,0,0),times=105),
      rep(c(0,0,1,0,0),times=8),
      rep(c(0,1,0,0,0),times=8),
      rep(c(0,1,0,1,1),times=1),
      rep(c(0,1,1,0,0),times=1),
      rep(c(0,1,1,1,0),times=7),
      rep(c(0,1,1,1,1),times=3),
      rep(c(1,0,0,0,0),times=9),
      rep(c(1,0,1,1,1),times=2),
      rep(c(1,1,1,1,0),times=2),
      rep(c(1,1,1,1,1),times=5))
J <- 5
I <- length(confusion.matrix) / J
x <- matrix(confusion.matrix,nrow=I,ncol=J,byrow=TRUE)

