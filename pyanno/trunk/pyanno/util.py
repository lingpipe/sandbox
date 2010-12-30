
def warn_missing_vals(varname,xs):
    missing = set(xs) - set(range(max(xs)+1))
    if len(missing) > 0:
        print "Missing values in ",varname,"=",missing

def list_copy(froms,tos,indexes):
    for i in indexes:
        tos[i] = froms[i]


def fill_vec(xs,y):
    i = 0
    while i < len(xs):
        xs[i] = y
        i += 1

def fill_mat(xs,y):
    i = 0
    while i < len(xs):
        fill_vec(xs[i],y)
        i += 1

def fill_tens(xs,y):
    i = 0
    while i < len(xs):
        fill_mat(xs[i],y)
        i += 1

def prob_norm(theta,indexes):
    Z = sum(theta)
    n = len(theta) - 1
    while n >= 0:
        theta[n] /= Z
        n -= 1


def alloc_vec(N,x=0.0):
    result = []
    n = 0
    while n < N:
        result.append(x)
        n += 1
    return result


def alloc_mat(M,N,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(alloc_vec(N,x))
        m += 1
    return result
                   
    
def alloc_tens(M,N,J,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(alloc_mat(N,J,x))
        m += 1
    return result
            
        
    
