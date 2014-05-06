#!/opt/local/bin/python2.7

from scapy.all import *

class Shenanigans:
	def __init__(self):
		pass

	def start(self):
		def probefilter (x):
			return True;
			#return x.haslayer(Dot11ProbeReq)

		def probefound (x):
			print x.summary()

		sniff(iface="en0", lfilter=probefilter, prn=probefound)

	

	


if __name__ == '__main__':
  Shenanigans().start();
