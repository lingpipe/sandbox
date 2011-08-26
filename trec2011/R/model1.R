#install.packages("rjags")
#install.packages("R2jags")
library("rjags")
library("R2jags")

dd1_tt1 <- matrix(scan("../data/munged/dd1_tt1.csv"), ncol=2, byrow=TRUE);
dd1 <- dd1_tt1[,1];
tt1 <- dd1_tt1[,2];

dd2_tt2_z2 <- matrix(scan("../data/munged/dd2_tt2_z2.csv"), ncol=3, byrow=TRUE);
dd2 <- dd2_tt2_z2[,1];
tt2 <- dd2_tt2_z2[,2];
z2 <- dd2_tt2_z2[,3];

ii1_jj1_y1 <- matrix(scan("../data/munged/ii1_jj1_y1.csv"), ncol=3, byrow=TRUE);
ii1 <- ii1_jj1_y1[,1];
jj1 <- ii1_jj1_y1[,2];
y1 <- ii1_jj1_y1[,3];

ii2_jj2_y2 <- matrix(scan("../data/munged/ii2_jj2_y2.csv"), ncol=3, byrow=TRUE);
ii2 <- ii2_jj2_y2[,1];
jj2 <- ii2_jj2_y2[,2];
y2 <- ii2_jj2_y2[,3];

# doc <- scan("../data/munged/doc.csv");
topic <- scan("../data/munged/topic.csv");
worker <- scan("../data/munged/worker.csv");

T <- length(topic);
J <- length(worker);

K1 <- length(ii1);
K2 <- length(ii2);
I1 <- max(ii1);
I2 <- max(ii2);

jags.data <- list("T", "J", 
             "I1", "tt1", "z1",
             "I2", "tt2", "z2",
             "K1", "y1", "ii1", "jj1",
             "K2", "y2", "ii2", "jj2");

jags.params <- c("alpha_pi","beta_pi", "pi",
                  "alpha0", "beta0", "theta0",
                  "alpha1", "beta1", "theta1",
                  "z1")

jags.inits <- c()

jags.fit <- jags(data=jags.data, inits=jags.inits, jags.params,
                 n.iter=10, model.file="../jags/model1.jags");

print(jags.fit);
plot(jagsfit);
#traceplot(jagsfit);                 

attach.jags(jags.fit);


