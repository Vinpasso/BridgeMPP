package bridgempp.message.formats.text;

import java.util.Arrays;

import org.jivesoftware.smackx.xhtmlim.XHTMLText;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class XHTMLXMPPMessageBody extends XHTMLMessageBody
{

	private String[] knownTags = new String[] {
			XHTMLText.A,
			XHTMLText.BLOCKQUOTE,
			XHTMLText.BR,
			XHTMLText.CITE,
			XHTMLText.CODE,
			XHTMLText.EM,
			XHTMLText.H,
			XHTMLText.HREF,
			XHTMLText.IMG,
			XHTMLText.LI,
			XHTMLText.OL,
			XHTMLText.P,
			XHTMLText.Q,
			XHTMLText.SPAN,
			XHTMLText.STRONG,
			XHTMLText.UL
	};
	
	public XHTMLXMPPMessageBody(String htmlText)
	{
		super(htmlText);
		htmlDocument.traverse(new NodeVisitor() {
			
			@Override
			public void head(Node node, int depth)
			{
				if(node instanceof Element)
				{
					Element element = (Element)node;
					if(!Arrays.stream(knownTags).anyMatch(e -> element.tagName().contains(htmlText)))
					{
						node.remove();
					}
				}
			}

			@Override
			public void tail(Node node, int depth)
			{
			}
		});
	}
	
	@Override
	public String getFormatName()
	{
		return "XHTML-XMPP";
	}

}
