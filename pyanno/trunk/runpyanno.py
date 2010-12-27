import pyanno
import pymc
import numpy

I = 10000
J = 5
K = 5
N = I*J 

Is = range(I)
Js = range(J)
Ks = range(K)
Ns = range(N)

beta = []
for k in Ks:
    beta.append(2)
prevalence = pymc.rdirichlet(beta).tolist()
prevalence.append(1.0-sum(prevalence)) # complete
category = []
for i in Is:
    category.append(pymc.rcategorical(prevalence).tolist())

#doh!  alpha set up to do three-way only

alpha = pyanno.alloc_mat(K,K)
for k1 in Ks:
    for k2 in Ks:
        alpha[k1][k2] = (5 - abs(k1 - k2))**2
accuracy = pyanno.alloc_tens(J,K,K)
for j in Js:
    for k in Ks:
        accuracy[j][k] = pymc.rdirichlet(alpha[k]).tolist()
        accuracy[j][k].append(1.0-sum(accuracy[j][k]))

item = []
anno = []
label = []
k = 0
for i in Is:
    for j in Js:
        item.append(i)
        anno.append(j)
        label.append(pymc.rcategorical(accuracy[j][category[i]]).tolist())
        k += 1

sample_prevalence = pyanno.alloc_vec(K)
for k in label:
    sample_prevalence[k] += 1
pyanno.prob_norm(sample_prevalence,Ks)

# k = 0
# for i in Is:
#     print "cat ",i,"=",category[i]
#     for j in Js:
#         print "     label ",j,"=",label[k]
#         k += 1

for j in Js:
    for k in Ks:
        print "acc ",j," ",k,"=",accuracy[j][k]
print "prevalence=",prevalence
print "sample prevalence=",sample_prevalence

delta = pyanno.alloc_vec(K)
epoch = 0
for (ll,prev,cat,acc) in pyanno.dawid_skene_mle_em(item,anno,label):
    if epoch > 1000: break
    print ""
    print "===================================="
    print "EPOCH=", epoch
    print "log likelihood=",ll
    print "prev=", prev
    for k in Ks:
        delta[k] = prev[k] - sample_prevalence[k]
    print "estimated_prev - sample_prev=",delta
    
    epoch += 1

print "prev=", prev
#print "cat=", cat
for j in Js:
    for k in Ks:
        print "acc=[",j,",",k,"]=",acc[j][k]
