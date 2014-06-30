#!/usr/bin/python2.7

from scapy.all import *
import sqlite3
import string
import getopt
import datetime
import time
import Queue
import thread

class ProbePacket:

	def __init__(self, packet):
		self.packet = packet;

# Sniffs all apparently valid wifi probes, using Scapy
class ProbeSniffer:
	def __init__(self):
		pass

	def makePacket(self,x):
		pass

	def packagePacket(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		mac = x.getlayer(Dot11).getfieldval("addr2")
		ssid = layer2[0].getfieldval("info")
		packet = {'mac':mac, 'ssid':ssid, 'raw': str(x.getlayer(Dot11))}
		#packet = {'mac':mac, 'ssid':ssid, 'raw': str(x)}
		return packet

	def getSSID(self, x):
		layer = x.getlayer(Dot11ProbeReq)
		layer2 = layer.getlayer(Dot11Elt)
		ssid = layer2[0].getfieldval("info")
		return ssid

	# Performs basic filtering:
	# filters corrupt packets
	# filters packets of excessive length (probe requests should be small, big ones mean problems)
	# filters packets with no SSID

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
		#packet = ProbePacket
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
				self.con.commit()
		except sqlite3.Error as e:
			print("Couldn't create db schema: {0}".format(e))
		

	def savePackets(self, packets):
		try:
			with self.con:
				cursor = self.con.cursor()
				rows = [(packet['mac'], packet['ssid'], packet['raw'], packet['time']) for packet in packets]
				cursor.executemany('''INSERT INTO PROBE_REQUESTS(mac, ssid, raw, time) VALUES(?,?,?,?)''', rows)
				self.con.commit()
		except sqlite3.Error as e:
			print("Couldn't save packet. {0}".format(e))


	def getSomePackets(self):
		try:
			with self.con:
				cursor = self.con.cursor()
				rows = cursor.execute('''SELECT {raw_name} from {table_name} ORDER BY {time_name} LIMIT 5'''.format(
										raw_name = SQLPacketStore.RAW_NAME,
										table_name = SQLPacketStore.PROBE_TABLE_NAME,
										time_name = SQLPacketStore.TIME_NAME))
				return [row[0] for row in rows]

		except sqlite3.Error as e:
			print("Could't retrieve packets. {0}".format(e))



class StorageStrategy:
	def store(x):
		pass


class Shenanigans:

	store = SQLPacketStore()
	probeQueue = Queue.Queue(10)

	def __init__(self):
		pass

	def probefound(self,x):
		#self.store.savePacket(x)
		self.probeQueue.put(x)
		print("Probe request: {mac} {ssid} {time} {raw}".format(**x))


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

	def start(self, iface):
		
		thread.start_new_thread(self.startSniffer, (iface,))
		#ProbeSniffer().start(self.probefound, iface)
		print("Sniffer thread started. Starting broadcast loop.")
		while True:
			packetsToSave = self.getAllInQueue()
			self.store.savePackets(packetsToSave)

			packets = self.store.getSomePackets()
			print("sending packets: {0}".format(packets))
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
  