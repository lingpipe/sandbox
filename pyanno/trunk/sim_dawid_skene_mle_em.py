import pyanno
import pymc
import numpy

# size constants for simulation
I = 10000
J = 5
K = 5
N = I*J 

Is = range(I)
Js = range(J)
Ks = range(K)
Ns = range(N)

# simulated params
beta = []
for k in Ks:
    beta.append(2)
prevalence = pymc.rdirichlet(beta).tolist()
prevalence.append(1.0-sum(prevalence)) # complete
print "prevalence.sim=",prevalence
category = []
for i in Is:
    category.append(pymc.rcategorical(prevalence).tolist())

alpha = pyanno.alloc_mat(K,K)
for k1 in Ks:
    for k2 in Ks:
        alpha[k1][k2] = (5 - abs(k1 - k2))**2
accuracy = pyanno.alloc_tens(J,K,K)
for j in Js:
    for k in Ks:
        accuracy[j][k] = pymc.rdirichlet(alpha[k]).tolist()
        accuracy[j][k].append(1.0-sum(accuracy[j][k]))

# simulated data
item = []
anno = []
label = []
for i in Is:
    for j in Js:
        item.append(i)
        anno.append(j)
        label.append(pymc.rcategorical(accuracy[j][category[i]]).tolist())
N = len(item)

# sampled params (i.e., MLEs from sampled data knowing true cats)
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

# EM loop from pyanno
epoch = 0
for (ll,prevalence_mle,category_mle,accuracy_mle) in pyanno.dawid_skene_mle_em(item,anno,label):
    if epoch > 10: break
    print "EPOCH={0:6d}  log likelihood={1:+10.4f}".format(epoch,ll)
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
    print "{0:2d}, {1:5.3f}, {2:5.3f}, {3:5.3f}, {4:+5.3f}, {5:+5.3f}".format(k,prevalence[k],prevalence_sample[k],prevalence_mle[k],prevalence[k]-prevalence_mle[k],prevalence_sample[k]-prevalence_mle[k])

print ""
print "ACCURACY ESTIMATES"
print "{0:>3s},{1:>2s},{2:>2s}, {3:>5s}, {4:>5s}, {5:>5s}, {6:>6s}, {7:>6s}".format("j","k1","k2","sim","samp","mle","d.sim","d.samp")
for j in Js:
    for k1 in Ks:
        for k2 in Ks:
            print "{0:3d},{1:2d},{2:2d}, {3:5.3f}, {4:5.3f}, {5:5.3f}, {6:+5.3f}, {7:+5.3f}".format(j,k1,k2,accuracy[j][k1][k2],accuracy_sample[j][k1][k2],accuracy_mle[j][k1][k2],accuracy[j][k1][k2]-accuracy_mle[j][k1][k2],accuracy_sample[j][k1][k2]-accuracy_mle[j][k1][k2])

