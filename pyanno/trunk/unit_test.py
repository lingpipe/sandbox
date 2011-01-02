from pyanno.kappa import *
from pyanno.multinom import *
from pyanno.util import *

import unittest

class TestUtil(unittest.TestCase):
    def test_vec_sum(self):
        self.assertEquals(0,vec_sum([]))
        self.assertEquals(1,vec_sum([1]))
        self.assertEquals(3,vec_sum([1,2]))
    def test_mat_sum(self):
        self.assertEquals(0,mat_sum([[]]))
        self.assertEquals(1,mat_sum([[1]]))
        self.assertEquals(3,mat_sum([[1,2]]))
        self.assertEquals(21,mat_sum([[1,2,3],[4,5,6]]))
    def test_prob_norm(self):
        theta = [0.2]
        prob_norm(theta)
        self.assert_prob_normed(theta)
    def assert_prob_normed(self,theta):
        self.assert_(len(theta) > 0)
        for theta_i in theta:
            self.assert_(theta_i >= 0.0)
            self.assert_(theta_i <= 1.0)
        self.assertAlmostEqual(1.0,vec_sum(theta),3)

class TestAgr(unittest.TestCase):
    def test_base(self):
        conf_mat = [1]
        self.assertEquals(1,1)

class TestAgr2(unittest.TestCase):
    def testbase2(self):
        self.assertEquals(2,2)

unittest.main()


