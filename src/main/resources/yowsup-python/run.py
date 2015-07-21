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
		return tuple(self.args[2].split(":"))

if __name__ == "__main__":
    BridgeMPPArgParser()
