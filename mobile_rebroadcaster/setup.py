#!/usr/bin/env python

from distutils.core import setup

setup(name='Shenanigans',
      version='0.1a',
      description='Identity Entropy: Electronic Countermeasures for Everyday Life',
      author='David Rueter',
      author_email='info@davidrueter.com',
      url='http://shenanigans.io',
      packages=['shenanigans'],
      requires=['sqlite3','scapy'],
      license='GPLv3',
      )