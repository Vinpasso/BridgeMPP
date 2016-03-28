package bridgempp.service.whatsapp;

import java.io.IOException;
import java.util.regex.Pattern;

import bridgempp.data.ExternalProcessService;
import bridgempp.messageformat.MessageFormat;
import bridgempp.servicecomponent.InputStreamToString;
import bridgempp.servicecomponent.MessageBodyBase64UTF8Decoder;
import bridgempp.servicecomponent.MessageBodyBase64UTF8Encoder;
import bridgempp.servicecomponent.MessageToCommandInterpreter;
import bridgempp.servicecomponent.MessageToFormattedString;
import bridgempp.servicecomponent.StringToMessageRegexNode;
import bridgempp.servicecomponent.StringToPrintStream;

public class WhatsappService extends ExternalProcessService
{

	@Override
	public void connect() throws IOException
	{
		super.connect();
		InputStreamToString reader = new InputStreamToString(process.getInputStream(), "Whatsapp Message Reader");
		Pattern parseRegex = Pattern.compile("\\[([\\d]*?)\\/([^\\(]*?)\\(([^()]*?)\\)\\]:\\[([^()]*?)]\\s*?(\\S+)");
		StringToMessageRegexNode regexParser = new StringToMessageRegexNode(parseRegex, this, MessageFormat.PLAIN_TEXT);
		reader.addOutput(regexParser);
		
		MessageBodyBase64UTF8Decoder decoder = new MessageBodyBase64UTF8Decoder();
		regexParser.addOutput(decoder);
		
		MessageToCommandInterpreter forward = new MessageToCommandInterpreter();
		decoder.addOutput(forward);
		
		incommingDAG.insertHead(reader);
		
		MessageBodyBase64UTF8Encoder encoder = new MessageBodyBase64UTF8Encoder();
		MessageToFormattedString formatter = new MessageToFormattedString();
		encoder.addOutput(formatter);
		
		StringToPrintStream printer = new StringToPrintStream(process.getOutputStream());
		formatter.addOutput(printer);
		
		outgoingDAG.insertHead(encoder);
	}

	@Override
	public void disconnect() throws IOException
	{
		super.disconnect();
	}

	@Override
	public String getName()
	{
		return "Whatsapp";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return MessageFormat.PLAIN_TEXT_ONLY;
	}
	
}
