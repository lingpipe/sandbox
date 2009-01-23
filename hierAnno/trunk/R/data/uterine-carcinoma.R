confusion.matrix <-
    c(rep(c(0,0,0,0,0,0,0), times=34),
      rep(c(0,0,0,0,1,0,0), times=2),
      rep(c(0,1,0,0,0,0,0), times=6),
      rep(c(0,1,0,0,0,0,1), times=1),
      rep(c(0,1,0,0,1,0,0), times=4),
      rep(c(0,1,0,0,1,0,1), times=5),
      rep(c(1,0,0,0,0,0,0), times=2),
      rep(c(1,0,1,0,1,0,1), times=1),
      rep(c(1,1,0,0,0,0,0), times=2),
      rep(c(1,1,0,0,0,0,1), times=1),
      rep(c(1,1,0,0,1,0,0), times=2),
      rep(c(1,1,0,0,1,0,1), times=7),
      rep(c(1,1,0,0,1,1,1), times=1),
      rep(c(1,1,0,1,0,0,1), times=1),
      rep(c(1,1,0,1,1,0,1), times=2),
      rep(c(1,1,0,1,1,1,1), times=3),
      rep(c(1,1,1,0,1,0,1), times=13),
      rep(c(1,1,1,0,1,1,1), times=5),
      rep(c(1,1,1,1,1,0,1), times=10),
      rep(c(1,1,1,1,1,1,1), times=16))
J <- 7
I <- length(confusion.matrix) / J
x <- matrix(confusion.matrix,nrow=I,ncol=J,byrow=TRUE)
