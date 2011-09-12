library('ggplot2');

kappa0 <- alpha_0 + beta_0;
phi0 <- alpha_0 / kappa0;

kappa1 <- alpha_1 + beta_1;
phi1 <- alpha_1 / kappa1;

kappaPi <- alpha_pi + beta_pi;
phiPi <- alpha_pi / kappaPi;

kappa0 <- kappa0[900:1100]
kappa1 <- kappa1[900:1100]
kappaPi <- kappaPi[900:1100]

phi0 <- phi0[900:1100]
phi1 <- phi1[900:1100]
phiPi <- phiPi[900:1100]


ylims = c(0,max(kappa0,max(kappa1,kappaPi)));
xlims = c(0,1);

plotter <- function(phi,kappa,title) {
  qp <- qplot(phi,kappa,
              xlim=xlims, ylim=ylims,
              alpha=I(1/15),
              xlab="phi", ylab="kappa", main=title)
  qp <- qp + opts(plot.title = theme_text(size = 12, face="bold", vjust=0.75));
}
spec_post <- plotter(phi0,kappa0,"Specificity Prior");
sens_post <- plotter(phi1,kappa1,"Sensitivity Prior");
pi_post <- plotter(phiPi,kappaPi,"Prevalence Prior");

vplayout <- function(x,y) {
 viewport(layout.pos.row = x, layout.pos.col = y);
}

grid.newpage()
pushViewport(viewport(layout = grid.layout(2,2)));
print(spec_post, vp = vplayout(1,1));
print(sens_post, vp = vplayout(1,2));
print(pi_post, vp = vplayout(2,1));
