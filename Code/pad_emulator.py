import time
import socket
import threading
from threading import Thread, current_thread
import subprocess				#accessing shutdown
from six.moves import input 			#temporary, to control flow
from zeroconf import ServiceBrowser, Zeroconf	#for service discovery nds
import uuid


server_address = ""
server_port  = 0



class Client(object):
	
	
	def __init__(self, server, port, connection_lost, padnum):
		self.server = server
		self.port = port
		self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.settimeout(100)
		self.connection_lost = connection_lost
		self.padnum = padnum
		try:
			self.sock.connect((self.server, self.port))
			listen = threading.Thread(target = self.acceptCommand)
			listen.start()
			#print ("pad ", self.padnum, " connected.")
		except:
			print ("Could not connect to server at address: ", self.server, "port: ", self.port)
			connection_lost.set()


		

	def acceptCommand(self):
		#uncomment for debug
		buffer = 1024
		cl = CommandList()
		while True:
			try:
				command = str(self.sock.recv(buffer).decode())
				if command:

					execute = threading.Thread(target = cl.call, args = (command, self.sock, self.connection_lost, self.padnum, ))
					execute.start()

				else:
					print ("pad ", self.padnum, " disconnected.")
					self.sock.close()
					self.connection_lost.set()
					return False

			except:
				print ("read error")
				self.connection_lost.set()
				self.sock.close()
				return False
		return True


class CommandList:
	def call(self,NUM, socket, connection_lost, padnum):
		
		NUM = str(NUM)
		print("Pad ", padnum, ":")
		if(NUM == "0"):
			print ("Received 0 - Connection Terminated\n")
			socket.close()

		elif(NUM == "1"):
			print ("Received 1 - Connection Established\n")
			socket.send(str.encode("1"))
			socket.send(str.encode("\n"))
			socket.send(str.encode(str(padnum)))
			socket.send(str.encode("\n"))

		elif(NUM == "2"):
			print ("Received 2 - Sensor On, Detecting Impacts\n")
			

		elif(NUM == "3"):
			print ("Received 3 - Target On\n")
			time.sleep(2)
			print ("HIT -sending 3")
			socket.send(str.encode("3"))
			socket.send(str.encode("\n"))
			
		elif(NUM == "4"):
			print ("Received 4 -Turning off sensor\n")

		else:
			print ("not recognized", NUM, "\n")
			#return "unkown"
		


class MyListener:

	def remove_service(self, zeroconf, type, name):
		#print("Service %s removed" % (name,))
		pass

	def add_service(self, zeroconf, type, name):
		global server_address, server_port
		info = zeroconf.get_service_info(type, name)
		#print("Service %s added, service info: %s" % (name, info))
		if info.name.find('skillcourtapp')  >= 0:         #if skillcourtapp is in name, save info to glob vars
			server_address = socket.inet_ntoa(info.address)
			server_port = info.port
			#print ("ServerAddress and Port Found: ",server_address, server_port)

class  Utilities(object):

	def findAppConnection(self, event_handler):
		#print ("looking for server address and port of Skill Court App")
		thread = threading.Thread(target = self.appBrowse, args = (event_handler,))
		thread.start()
		#print ("App browsing thread started")

	def appBrowse(self, event_handler):
		zeroconf = Zeroconf()
		listener = MyListener()
		browser = ServiceBrowser(zeroconf, "_http._tcp.local.", listener)
		while server_port == 0:
			pass
		event_handler.set()
		zeroconf.close()
		#print ("closed zeroconf")
		return True

if __name__ == "__main__":
	server_found = threading.Event()
	connection_lost = threading.Event()
	

	while True:
		Utilities().findAppConnection(server_found)
		print ("waiting for discovery")
		server_found.wait()
		pad1 = threading.Thread(target = Client, args = (server_address, server_port, connection_lost, 100, ))
		pad2 = threading.Thread(target = Client, args = (server_address, server_port, connection_lost, 200, ))
		pad3 = threading.Thread(target = Client, args = (server_address, server_port, connection_lost, 300, ))
		pad4 = threading.Thread(target = Client, args = (server_address, server_port, connection_lost, 400, ))
	
		pad1.start()
		pad2.start()
		pad3.start()
		pad4.start()
	
		pad1.join()
		pad2.join()
		pad3.join()
		pad4.join()

		connection_lost.wait()
		connection_lost.clear()
		server_found.clear()
		server_address = ""
		server_port = 0

