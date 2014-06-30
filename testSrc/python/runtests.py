#!/opt/local/bin/python2.7
import sys, os
import unittest

sys.path.append("../../src/python")

import tests

suite = unittest.TestLoader()
suite = suite.loadTestsFromModule(tests)
unittest.TextTestRunner().run(suite)

