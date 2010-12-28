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
    if epoch > 100: break
    print "EPOCH={0:6d}  log likelihood={1:+10.4f}".format(epoch,ll)
    epoch += 1

for k in Ks:
    print "prev*[{0:2d}]={1:5.3f} prev[]={2:5.3f}  delta={3:+5.3f}".format(k,prev[k],prevalence[k],prev[k]-prevalence[k])
for j in Js:
    for k1 in Ks:
        for k2 in Ks:
            print "acc*[{0:3d},{1:2d},{2:2d}]={3:5.3f}  acc[]={4:5.3f}  delta={5:+5.3f}".format(j,k1,k2,acc[j][k1][k2],accuracy[j][k1][k2],acc[j][k1][k2]-accuracy[j][k1][k2])
#for i in Is:
#    for k in Ks:
#        print "E[cat[",i,",",k,"]|...]=",cat[i][k]

