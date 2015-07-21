import sys, argparse, yowsup, logging
class BridgeMPPArgParser():
	def __init__(self):
		self.args = sys.argv
		if(len(self.args) > 1):
			del self.args[0]
		from yowsupclient.stack import BridgeMPPCliStack
		credentials = self._getCredentials()
		if not credentials:
			print("Error: You must specify a configuration method")
			sys.exit(1)
		stack = BridgeMPPCliStack(credentials, False)
		stack.start()
		  
	def _getCredentials(self):
		config = self.getConfig(self.args[0])
		assert "password" in config and "phone" in config, "Must specify at least phone number and password in config file"
		return config["phone"], config["password"]
			
	def getConfig(self, config):
		try:
			f = open(config)
			out = {}
			for l in f:
				 line = l.strip()
				 if len(line) and line[0] not in ('#',';'):
					  prep = line.split('#', 1)[0].split(';', 1)[0].split('=', 1)
					  varname = prep[0].strip()
					  val = prep[1].strip()
					  out[varname.replace('-', '_')] = val
			return out
		except IOError:
			print("Invalid config path: %s" % config)
			sys.exit(1)

if __name__ == "__main__":
    BridgeMPPArgParser()
