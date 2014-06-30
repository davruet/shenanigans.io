
from shenanigans import SQLPacketStore
import datetime

store = SQLPacketStore()
packet = {'ssid': 'somessid', 'mac':'DEADBEEF', 'raw':"SOMETHING", 'time': datetime.datetime.utcnow()}
store.savePacket(packet)

