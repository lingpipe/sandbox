# Library Module

def hello(a="world"):
    print "hello", a
    

def emDawidSkene(alpha,   # float[K][K]
                 beta,    # float[K]
                 item,    # int[N]
                 anno,    # int[N]
                 label):  # int[N]
    I = max(item)
    J = max(anno)
    K = max(label)
    N = len(item)

    Is = range(0,I-1)
    Js = range(0,J-1)
    Ks = range(0,K-1)

    if len(alpha) != K:
        raise ValueError("len(alpha) != K")
    for k in Ks:
        if len(alpha[k]) != K:
            raise ValueError("len(alpha[k]) != K")
    if len(beta) != K:
        raise ValueError("len(beta) != K")
    if len(anno) != N:
        raise ValueError("len(anno) != N")
    if len(label) != N:
        raise ValueError("len(label) != N")


    prevalence = []          # double[K]
    category = [ [] ]        # double[I][K]
    accuracy = [ [ [] ] ]    # double[J][K][K]
    for k in Ks:
        prevalence[k] = 1/K
    for i in Is:
        for k in Ks:
            category[i][k] = 1/K
    for j in Js:
        for k1 in Ks:
            for k2 in Ks:
                accuracy[j][k1][k2] = 0.7 if k1 == k2 else 1/(K-1)
          
    for params in emLoop(alpha,beta,item,anno,label,
                         I,J,K,N,Is,Js,Ks,
                         prevalence,category,accuracy):
        yield params


def emLoop(alpha,beta,item,anno,label,
           I,J,K,N,Is,Js,Ks,
           prevalence,category,accuracy):
    for n in [1,2,3]:
        yield n

                

        
            
        
    
