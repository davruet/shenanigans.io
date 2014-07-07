#!/usr/bin/python2.7
""" 
Shenanigans, version 0.1a 

A note on code standards and style: this code should be clear and concise. Not only
should it be functional and maintainable, but it also serves as a teaching tool.
Please keep beggining and intermediate programmers in mind when making changes.

Please also feel free to throw educational comments about the language in as well,
if you find a "teaching moment."


Pythonistas, please let me know if you find some abuses in this code -- Java is my first language.

-David

"""

from scapy.all import *
#from datetime import timedelta
import sqlite3
import string
import getopt
import datetime
import time
import Queue
import thread


""" Sniffs all apparently valid wifi probes, using Scapy """
class ProbeSniffer:
	def __init__(self):
		pass

	""" Creates a dictionary containing the mac, ssid, and raw data of this packet. 
	:param x: the packet"""
	def packagePacket(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		mac = x.getlayer(Dot11).getfieldval("addr2")
		ssid = layer2[0].getfieldval("info")
		packet = {'mac':mac, 'ssid':ssid, 'raw': str(x.getlayer(Dot11)), 'rssi' : self.extractRSSI(x)}
		#packet = {'mac':mac, 'ssid':ssid, 'raw': str(x)}
		return packet

	def extractRSSI(self, x):
		return -(256-ord(x.notdecoded[-4:-3]))

	""" Extracts the SSID from the supplied probe request
	:param x: the packet """
	def getSSID(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		ssid = layer2[0].getfieldval("info")
		return ssid

	""" Performs basic filtering:
	* filters corrupt packets
	* filters packets of excessive length (probe requests should be small, big ones mean problems)
	* filters packets with no SSID 
	"""

	def probefilter (self, x):
		#return True;
		if not x.haslayer(Dot11ProbeReq): 
			return False
		ssid = self.getSSID(x)
		return ssid > "" and all(c in string.printable for c in ssid)
		# TODO - only return probes with valid checksums
		#if ssid > "" and all(c in string.printable for c in ssid):
			#print ("\t".join([mac,ssid]))
		#	return True
		#else:
		#	return False

	def probefound (self,x):
		packet = self.packagePacket(x)
		packet['time'] = datetime.datetime.now()
		self.found(packet)
		
	def start(self, found, iface="wlan0"):
		self.found = found
		sniff(iface=iface, lfilter=self.probefilter, prn=self.probefound)

	
class SQLPacketStore:

	PROBE_TABLE_NAME = 'PROBE_REQUESTS'
	MAC_NAME = 'mac'
	SSID_NAME = 'ssid'
	RAW_NAME = 'raw'
	TIME_NAME = 'time'
	DB_NAME = 'probe_requests.db'

	def createIndex(self, colname, cursor):
		sql = '''CREATE INDEX IF NOT EXISTS {table_name}{col_name}_ix ON
									{table_name} ({col_name})'''.format(
										table_name = SQLPacketStore.PROBE_TABLE_NAME,
										col_name = colname)
		cursor.execute(sql)

	def __init__(self):
		self.con = sqlite3.connect(SQLPacketStore.DB_NAME)
		self.con.text_factory = str
		self.con.isolation_level = None
		try:
			with self.con:
				cursor = self.con.cursor()
				cursor.execute('''CREATE TABLE IF NOT EXISTS
									{table_name}(id INTEGER PRIMARY KEY AUTOINCREMENT, {mac_name} TEXT,
								    {ssid_name} TEXT, {raw_name} BLOB, {time_name} timestamp)'''.format(
								    	table_name = SQLPacketStore.PROBE_TABLE_NAME,
								    	mac_name = SQLPacketStore.MAC_NAME,
								    	ssid_name = SQLPacketStore.SSID_NAME,
								    	raw_name = SQLPacketStore.RAW_NAME,
								    	time_name = SQLPacketStore.TIME_NAME))

				self.createIndex(SQLPacketStore.MAC_NAME, cursor)
				self.createIndex(SQLPacketStore.SSID_NAME, cursor)
				self.createIndex(SQLPacketStore.TIME_NAME, cursor)
		except sqlite3.Error as e:
			print("Couldn't create db schema: {0}".format(e))
		

	def savePackets(self, packets):
		try:
			with self.con:
				cursor = self.con.cursor()
				rows = [(packet['mac'], packet['ssid'], packet['raw'], packet['time']) for packet in packets]
				cursor.executemany('''INSERT INTO PROBE_REQUESTS(mac, ssid, raw, time) VALUES(?,?,?,?)''', rows)
		except sqlite3.Error as e:
			print("Couldn't save packet. {0}".format(e))


	""" Get a group of packets. """
	def getSomePackets(self, packetcount):

		try:
			with self.con:
				cursor = self.con.cursor()
				endTime = datetime.datetime.now() - datetime.timedelta(hours=0)
				rows = cursor.execute('''SELECT {raw_name} from {table_name}
											WHERE {time_name} < ?
				 ORDER BY RANDOM() LIMIT 30'''.format(
										raw_name = SQLPacketStore.RAW_NAME,
										table_name = SQLPacketStore.PROBE_TABLE_NAME,
										time_name = SQLPacketStore.TIME_NAME), (endTime,)) # common gotcha - requires a tuple with only one value 
				return [row[0] for row in rows]

		except sqlite3.Error as e:
			print("Could't retrieve packets. {0}".format(e))

""" 
Top-level controller that creates a sniffer, persists the results, and periodically
queries the probe store and rebroadcasts packets.
"""
class Shenanigans:

	store = SQLPacketStore()
	probeQueue = Queue.Queue(10)

	def __init__(self):
		pass

	"""
	Adds a packet to the queue of items to be persisted.
	:param x: the packet
	"""
	def probefound(self,x):
		#self.store.savePacket(x)
		self.probeQueue.put(x)
		print("Saving probe request: {mac} {ssid} {time}".format(**x))


	"""
	Empties the queue into a new collection
	"""
	def getAllInQueue(self):
		empty = False
		packets = []
		while not empty:
			try:
				packet = self.probeQueue.get(False)
				packets.append(packet)
			except Queue.Empty:
				empty = True
		return packets

	def startSniffer(self, iface):
		ProbeSniffer().start(self.probefound, iface)

	def persistQueue(self):
		packetsToSave = self.getAllInQueue()
		if packetsToSave:
			self.store.savePackets(packetsToSave)

	def broadcastProbeRequest(self, probe):
		
		probePacket =  RadioTap() / Dot11(probe) 
		print("sending probe: {0}".format(probePacket.summary()))
		sendp(probePacket, iface=self.iface) # TODO - decide how many of these we need to send.
		# todo - random sleep time between packets?

	def start(self, iface):
		self.iface = iface
		thread.start_new_thread(self.startSniffer, (iface,)) # common gotcha - requires a tuple with only one value 
		#ProbeSniffer().start(self.probefound, iface)
		print("Sniffer thread started. Starting broadcast loop.")

		while True:
			self.persistQueue()

			probeRequests = self.store.getSomePackets(50)
			map(self.broadcastProbeRequest, probeRequests)
			time.sleep(1)	

def printUsageAndExit():
	print 'shenanigans.py -i <interface>'
	sys.exit(2)

def main(argv):

	try:
		opts, args = getopt.getopt(argv,"i:")
	except getopt.GetoptError:
		printUsageAndExit()

	iface = None
	for opt, arg in opts:
		if opt == '-i':
			print ("starting shenanigans on {0}".format(arg) )
			iface = arg
	if iface == None:
		print("No interface specified -- use the -i option.")
		printUsageAndExit()

	Shenanigans().start(iface);

if __name__ == '__main__':
	main(sys.argv[1:])
  
