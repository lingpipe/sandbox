library('ggplot2')

num_labels <- rep(0,J);
for (j in 1:J) {
  num_labels[j] <- sum(jj1 == j) + sum(jj2 == j)
}
ranked_by_labels <- rank(num_labels);

plotter <- function(j) {
  qp <- qplot(theta0[850:1150,j],theta1[850:1150,j],xlim=c(0,1),ylim=c(0,1),xlab="spec",ylab="sens",
        main=sprintf("%d (N = %d)",j,num_labels[j]), alpha=I(1/20));
  qp <- qp + scale_x_continuous(breaks=c(0.0,0.5,1.0), limits=c(0,1));
  qp <- qp + scale_y_continuous(breaks=c(0.0,0.5,1.0), limits=c(0,1));
  qp <- qp + opts(plot.title = theme_text(size = 9, face="italic", vjust=0.75));
  qp <- qp + opts(axis.text.x = theme_text(size=8, colour="gray"));
  qp <- qp + opts(axis.text.y = theme_text(size=8, colour="gray"));
  qp <- qp + opts(axis.title.x = theme_text(size=9));
  qp <- qp + opts(axis.title.y = theme_text(size=9, angle=90));
  qp <- qp + coord_equal();
  qp;
}


vplayout <- function(x,y) {
  viewport(layout.pos.row = x, layout.pos.col = y);
}

grid.newpage()
N1 = 4;
N2 = 4;
pushViewport(viewport(layout = grid.layout(N1,N2)));



idxs <- c(133, # 5
            5, # 8
          617, # 13
          400, #26
          683, # 42
          441, # 82
          705, # 138
          684, # 252
          486, # 332
          275, # 408
#          663, # 531
          2, # 636
          667, #924
#          1, # 1221
          3, # 1552
          661,  # 1958
            30, # 3255
#           29, # 4945
          38 #7438
)        
m1 <- 1;
for (n1 in 1:N1) {
  for (n2 in 1:N2) {
    print(plotter(idxs[m1]),
          vp = vplayout(n1,n2));
    m1 <- m1 + 1;
  }
}

