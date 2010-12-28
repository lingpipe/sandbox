import pyanno

# Simulated Sizes
I = 10000
J = 5
K = 5
N = I*J 

Is = range(I)
Js = range(J)
Ks = range(K)
Ns = range(N)

# Simulate Full Data Set
(prevalence,category,accuracy,item,anno,label) = pyanno.sim_dawid_skene(I,J,K)

# Calculate Sample Params
prevalence_sample = pyanno.alloc_vec(K)
for k in category:
    prevalence_sample[k] += 1
pyanno.prob_norm(prevalence_sample,Ks)

accuracy_sample = pyanno.alloc_tens(J,K,K)
for n in Ns:
    accuracy_sample[anno[n]][category[item[n]]][label[n]] += 1
for j in Js:
    for k in Ks:
        pyanno.prob_norm(accuracy_sample[j][k],Ks)

# EM Fit
print "RUNNING EM"
MAX_EPOCHS = 500
EPSILON = 0.001
epoch = 0
log_likelihood_curve = []
diff = -1
for (ll,prevalence_mle,category_mle,accuracy_mle) in pyanno.dawid_skene_mle_em(item,anno,label):
    log_likelihood_curve.append(ll)
    if epoch > MAX_EPOCHS:
        break
    if len(log_likelihood_curve) > 10:
        diff = ll - log_likelihood_curve[epoch-10]
        if abs(diff) < EPSILON:
            break
    print "  epoch={0:6d}  log likelihood={1:+10.4f}   diff={2:8.4f}".format(epoch,ll,diff)
    print "prev_sample=",prevalence_sample
    print "prev_mle=",prevalence_mle
    epoch += 1

# print basics
print ""
print "SIZES"
print "I=",I
print "J=",J
print "K=",K
print "N=",N


# print resulting estimates
print ""
print "PREVALENCE ESTIMATES"
print "{0:>2s}, {1:>5s}, {2:>5s}, {3:>5s}, {4:>6s}, {5:>6s}".format("k","sim","samp","MLE","d.sim","d.samp")
for k in Ks:
    print "{0:2d}, {1:5.3f}, {2:5.3f}, {3:5.3f}, {4:+5.3f}, {5:+5.3f}".format(k,prevalence[k],prevalence_sample[k],prevalence_mle[k],prevalence_mle[k]-prevalence[k],prevalence_mle[k]-prevalence_sample[k])

print ""
print "ACCURACY ESTIMATES"
print "{0:>3s},{1:>2s},{2:>2s}, {3:>5s}, {4:>5s}, {5:>5s}, {6:>6s}, {7:>6s}".format("j","k1","k2","sim","samp","mle","d.sim","d.samp")
for j in Js:
    for k1 in Ks:
        for k2 in Ks:
            print "{0:3d},{1:2d},{2:2d}, {3:5.3f}, {4:5.3f}, {5:5.3f}, {6:+5.3f}, {7:+5.3f}".format(j,k1,k2,accuracy[j][k1][k2],accuracy_sample[j][k1][k2],accuracy_mle[j][k1][k2],accuracy_mle[j][k1][k2]-accuracy[j][k1][k2],accuracy_mle[j][k1][k2]-accuracy_sample[j][k1][k2])

