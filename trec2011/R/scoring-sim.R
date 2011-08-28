

score <- function(y, yHat) {
  tp = sum(y * yHat);
  fn = sum(y * (1 - yHat));
  tn = sum((1 - y) * (1 - yHat));
  fp = sum((1 - y) * yHat);
  rec = tp/(tp + fn);
  prec = tp/(tp + fp);
  spec = tn/(tn + fp);
  print(sprintf("rec=%5.3f prec=%5.3f  sens=%5.3f  spec=%5.3f",
                rec, prec, rec, spec),quote=F);
}

I = 10000;
yHat = runif(I);
y = rep(1.0,I);
for (i in 1:I) {
  y[i] = rbinom(1,1,yHat[i]);
}
print("",quote=F);
print("Probability Score",quote=F);
score(y,yHat);

print("",quote=F);
yHatQ = yHat > 0.5;
print("Quantized Score",quote=F);
score(y,yHatQ);


