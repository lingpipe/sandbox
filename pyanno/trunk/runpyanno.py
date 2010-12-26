import pyanno
import pymc
import numpy

I = 100
J = 3
K = 4
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

alpha = []
for k1 in Ks:
    alpha.append([])
    for k2 in Ks:
        alpha[k1].append((5 - abs(k1 - k2))**2)
accuracy = []
for j in Js:
    accuracy.append([])
    for k in Ks:
        accuracy[j].append(pymc.rdirichlet(alpha[k]).tolist())
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

epoch = 0
for (ll,prev,cat,acc) in pyanno.em_ds_prior(item,anno,label,alpha,beta):
    if epoch > 1000: break
    print ""
    print "===================================="
    print "EPOCH=", epoch
    print "log likelihood=",ll
    print "prev=", prev
    epoch += 1

print "prev=", prev
#print "cat=", cat
for j in Js:
    for k in Ks:
        print "acc=[",j,",",k,"]=",acc[j][k]
