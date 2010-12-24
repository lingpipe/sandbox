import libcurate
import numpy
import pymc

I = 200
J = 5
K = 3   
N = I*J 

Is = range(I)
Js = range(J)
Ks = range(K)
Ns = range(N)

beta = [ 2, 2, 2 ]
prevalence = pymc.rdirichlet(beta).tolist()
prevalence.append(1.0-sum(prevalence)) # complete
category = []
for i in Is:
    category.append(pymc.rcategorical(prevalence).tolist())

alpha = [ [ 16, 4, 1 ],
          [ 2, 16, 2 ],
          [ 1, 4, 16 ] ]
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
for (prev,cat,acc) in libcurate.em_dawid_skene(alpha,beta,item,anno,label):
    if epoch > 20: break
    print ""
    print "===================================="
    print "EPOCH=", epoch
    print "prev=", prev
    print "cat=", cat
    print "acc=", acc
    epoch += 1
