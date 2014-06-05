#/opt/local/bin/python2.7

from scapy.all import *
import sqlite3


class ProbePacket:

	def __init__(self, packet):
		self.packet = packet;

# Sniffs all apparently valid wifi probes, using Scapy
class ProbeSniffer:
	def __init__(self):
		pass

	def makePacket(x):

	def getPacketDetails(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		mac = x.getlayer(Dot11).getfieldval("addr2")
		ssid = layer2[0].getfieldval("info")
		return (mac,ssid)

	
	def probefilter (x):
			#return True;
			if not x.haslayer(Dot11ProbeReq) return False
			
			return ssid > "" and all(c in string.printable for c in ssid)
			#if ssid > "" and all(c in string.printable for c in ssid):
				#print ("\t".join([mac,ssid]))
			#	return True
			#else:
			#	return False

	def probefound (x):
		packet = ProbePacket
		found.probe()
		
	def start(self, found, iface="mon0"):
		self.found = found
		sniff(iface=iface, lfilter=probefilter, prn=probefound)

	
class SQLPacketStore:
	def __init__(self):
		self.con = sqlite3.connect(":memory:")
		self.con.isolation_level = None
		

	def initTables():

	def refreshTable():



class StorageStrategy:
	def store(x):


class Shenanigans:
	def __init__(self):

	def start(self):



if __name__ == '__main__':
  Shenanigans().start();
  