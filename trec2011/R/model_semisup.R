#install.packages("rjags")
#install.packages("R2jags")
library("rjags")
library("R2jags")

print("reading dd1,tt1, length=2*I1", quote=FALSE);
dd1_tt1 <- matrix(scan("../data/munged/dd1_tt1.csv", sep=","), 
                  ncol=2, byrow=TRUE);
dd1 <- dd1_tt1[,1];
tt1 <- dd1_tt1[,2];

print("reading dd2,tt2, length=3*I2", quote=FALSE);
dd2_tt2_z2 <- matrix(scan("../data/munged/dd2_tt2_z2.csv", sep=","), 
                     ncol=3, byrow=TRUE);
dd2 <- dd2_tt2_z2[,1];
tt2 <- dd2_tt2_z2[,2];
z2 <- dd2_tt2_z2[,3];

print("reading ii1,jj1,y1, length=3*K1", quote=FALSE);
ii1_jj1_y1 <- matrix(scan("../data/munged/ii1_jj1_y1.csv", sep=","), 
                     ncol=3, byrow=TRUE);
ii1 <- ii1_jj1_y1[,1];
jj1 <- ii1_jj1_y1[,2];
y1 <- ii1_jj1_y1[,3];

print("reading ii2,jj2,y2, length=3*K2", quote=FALSE);
ii2_jj2_y2 <- matrix(scan("../data/munged/ii2_jj2_y2.csv",sep=","),
                     ncol=3, byrow=TRUE);
ii2 <- ii2_jj2_y2[,1];
jj2 <- ii2_jj2_y2[,2];
y2 <- ii2_jj2_y2[,3];

print("reading doc symbols", quote=FALSE);
doc <- matrix(scan("../data/munged/doc_sym.csv", what="character", sep=","),
              ncol=2,byrow=T);

print("reading topic symbols, length=T", quote=FALSE);
topic <- matrix(scan("../data/munged/topic_sym.csv", what="character", sep=","),
                ncol=2,byrow=T);

print("reading worker symbols, length=J", quote=FALSE);
worker <- matrix(scan("../data/munged/worker_sym.csv", what="character",sep=","),
                 ncol=2,byrow=T);

T <- length(topic[,1]);
J <- length(worker[,1]);

K1 <- length(ii1);
K2 <- length(ii2);
I1 <- max(ii1);
I2 <- max(ii2);

print(sprintf("#topic= T =%d",T),quote=FALSE); 
print(sprintf("#worker= J = %d",J),quote=FALSE); 
print(sprintf("#doc/topic pairs=%d",I1+I2),quote=FALSE); 
print(sprintf("#doc/topic pairs w/o truth = I1 = %d",I1),quote=FALSE);
print(sprintf("#doc/topic pairs w truth = I2 = %d",I2),quote=FALSE);
print(sprintf("#judgments w/o truth = K1 = %d",K1),quote=FALSE); 
print(sprintf("#judgments w truth = K2 = %d",K2),quote=FALSE); 

z1_voted <- rep(0.0,I1);
z1_sum <- rep(0.0,I1);
for (k1 in 1:K1) {
    z1_voted[ii1[k1]] <- z1_voted[ii1[k1]] + y1[k1];
    z1_sum[ii1[k1]] <- z1_sum[ii1[k1]] + 1;
}
for (i in 1:I1) {
    z1_voted[i] <- z1_voted[i] / z1_sum[i];
}

# init to votes;  could make this random by vote
z1_init <- rep(0.0,I1)
for (i in 1:I1) {
    if (z1_voted[i] > 0.5) {
        z1_init[i] <- 1;
    } else if (z1_voted[i] == 0.5) {
        z1_init[i] <- rbinom(1,1,0.5);
    } else { # not necessary given rep() above
        z1_init[i] <- 0;
    }
}

jags.data <- list("T", "J", 
             "I1", "tt1",
             # "I2", "tt2", # no sample because not prop to prevalence
             "z2",
             "K1", "y1", "ii1", "jj1",
             "K2", "y2", "ii2", "jj2");

jags.params <- c("alpha_pi","beta_pi", "pi",
                  "alpha_0", "beta_0", "theta0",
                  "alpha_1", "beta_1", "theta1",
                  "z1");

jags.inits <- function() {
    list("phi_pi"=0.5, "kappa_pi"=4.0, "pi"=rep(0.25,T),
         "phi_0"=0.8, "kappa_0"=4.0, "theta0"=rep(0.8,J),
          # submitted run had "pi_1" error, so went with JAGS inits
         "phi_1"=0.8, "kappa_1"=4.0, "theta1"=rep(0.8,J),
         "z1"=z1_init
          );
}

jags.fit <- jags(data=jags.data, inits=jags.inits, jags.params,
                n.iter=2000,
                n.burnin=1000,
                n.thin=1,
                model.file="../jags/model_semisup.jags");

print(jags.fit);
plot(jags.fit);
#traceplot(jags.fit);                 

attach.jags(jags.fit);


