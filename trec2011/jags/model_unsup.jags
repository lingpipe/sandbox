model {
    for (t in 1:T) {
        pi[t] ~ dbeta(alpha_pi,beta_pi);
    }
    for (j in 1:J) {
        theta0[j] ~ dbeta(alpha_0,beta_0);
	theta1[j] ~ dbeta(alpha_1,beta_1);
    }
    for (i1 in 1:I1) {
        z1[i1] ~ dbern(pi[tt1[i1]]);
    }
    for (k1 in 1:K1) {
        y1[k1] ~ dbern(z1[ii1[k1]] * theta1[jj1[k1]] + 
                         (1 - z1[ii1[k1]]) * (1 - theta0[jj1[k1]]));
    }
    for (k2 in 1:K2) {
        y2[k2] ~ dbern(z2[ii2[k2]] * theta1[jj2[k2]] + 
                         (1 - z2[ii2[k2]]) * (1 - theta0[jj2[k2]]));
    }
    for (i2 in 1:I2) {
        z2[i2] ~ dbern(pi[tt2[i2]]);
    }
    phi_pi ~ dbeta(1,1);
    kappa_pi ~ dpar(1.5,0.1);
    alpha_pi <- kappa_pi * phi_pi;
    beta_pi <- kappa_pi * (1 - phi_pi);

    phi_0 ~ dbeta(1,1);
    kappa_0 ~ dpar(1.5,0.1);
    alpha_0 <- kappa_0 * phi_0;
    beta_0 <- kappa_0 * (1 - phi_0);

    phi_1 ~ dbeta(1,1);
    kappa_1 ~ dpar(1.5,0.1);
    alpha_1 <- kappa_1 * phi_1;
    beta_1 <- kappa_1 * (1 - phi_1);
}



