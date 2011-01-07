import pyhi.greet
import unittest

class TestPyhiGreet(unittest.TestCase):
    def test_hello(self):
        self.assertEquals("Hello, Dave",
                          pyhi.greet.hello())
        self.assertEquals("Hello, Bob",
                          pyhi.greet.hello("Bob"))
        self.assertEquals("Hello, John Doe",
                          pyhi.greet.hello(second="Doe", first="John"))

# required idiom to run unit tests when run as script
if __name__ == '__main__':
    unittest.main()
