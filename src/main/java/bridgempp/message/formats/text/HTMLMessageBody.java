package bridgempp.message.formats.text;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;


public class HTMLMessageBody extends MarkupTextMessageBody
{
	protected Document htmlDocument;

	public HTMLMessageBody(String htmlText)
	{
		super();
		htmlDocument = Jsoup.parse(htmlText);
	}
	
	@Override
	public String getText()
	{
		return htmlDocument.outerHtml();
	}

	@Override
	public String getFormatName()
	{
		return "HTML";
	}

	private static HtmlToPlainText converter = new HtmlToPlainText();
	
	public String formatToPlainText()
	{
		return converter.getPlainText(htmlDocument);
	}

}
