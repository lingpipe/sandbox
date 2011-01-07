import unittest
import pyhi.greet

class TestPyhiGreet(unittest.TestCase):
    def test_hello(self):
        self.assertEquals("Hello, Dave",
                          pyhi.greet.hello())
        self.assertEquals("Hello, Bob",
                          pyhi.greet.hello("Bob"))
        self.assertEquals("Hello, John Doe",
                          pyhi.greet.hello(second="Doe", first="John"))

# conditional runs tests if this file called as script (allows import w/o run)
if __name__ == '__main__':
    unittest.main()

