package bridgempp.message.formats;

import org.jsoup.nodes.Document.OutputSettings.Syntax;

public class XHTMLMessageBody extends HTMLMessageBody
{

	public XHTMLMessageBody(String htmlText)
	{
		super(htmlText);
		htmlDocument.outputSettings().syntax(Syntax.xml);
	}

	@Override
	public String getFormatName()
	{
		return "XHTML";
	}
	
}
