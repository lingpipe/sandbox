submission <- matrix(nrow=I1+I2,ncol=4);

for (i in 1:I1) {
    submission[i,1] = dd1_tt1[i,1];
    submission[i,2] = dd1_tt1[i,2];
    submission[i,4] = mean(z1[,i]);
  }
for (i in 1:I2) {
    submission[I1+i,1] = dd2_tt2_z2[i,1];
    submission[I1+i,2] = dd2_tt2_z2[i,2];
    submission[I1+i,4] = z2[i];
}

for (t in 1:T) {
  idx = (submission[,2] == t); # indexes of doc/topic pairs for topic t
  idx_len = sum(idx);
  submission[idx,3] = ( (1 + idx_len)                   # reverses low-to-igh
                        - rank(submission[idx,4],
                               ties.method="random") ); # random order for ties
}

for (i in 1:1000) {
  print(cat(submission[i,],sep="\t"),quote=F)
}

write(t(submission), file="lingpipe_semisup.csv", 
      ncolumns=4, sep="\t");

submission_q = submission;
submission_q[,4] = (submission[,4] > 0.5);

write(t(submission_q),
      file="lingpipe_semisup_binary.csv",
      ncolumns=4, sep="\t");


