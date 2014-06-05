#!/usr/bin/python2.7

from scapy.all import *
import sqlite3
import string


class ProbePacket:

	def __init__(self, packet):
		self.packet = packet;

# Sniffs all apparently valid wifi probes, using Scapy
class ProbeSniffer:
	def __init__(self):
		pass

	def makePacket(self,x):
		pass

	def getPacketDetails(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		mac = x.getlayer(Dot11).getfieldval("addr2")
		ssid = layer2[0].getfieldval("info")
		return (mac,ssid)

	
	def probefilter (self, x):
		#return True;
		if not x.haslayer(Dot11ProbeReq): 
			return False
		details = self.getPacketDetails(x)
		ssid = details[1]
		return ssid > "" and all(c in string.printable for c in ssid)
		#if ssid > "" and all(c in string.printable for c in ssid):
			#print ("\t".join([mac,ssid]))
		#	return True
		#else:
		#	return False

	def probefound (self,x):
		#packet = ProbePacket
		self.found(x)
		
	def start(self, found, iface="wlan0"):
		self.found = found
		sniff(iface=iface, lfilter=self.probefilter, prn=self.probefound)

	
class SQLPacketStore:
	def __init__(self):
		self.con = sqlite3.connect(":memory:")
		self.con.isolation_level = None
		

	def initTables():
		pass

	def refreshTable():
		pass



class StorageStrategy:
	def store(x):
		pass


class Shenanigans:

	def __init__(self):
		pass

	def probefound(self,x):
		print(x.summary())

	def start(self):
		ProbeSniffer().start(self.probefound)	



if __name__ == '__main__':
  Shenanigans().start();
  