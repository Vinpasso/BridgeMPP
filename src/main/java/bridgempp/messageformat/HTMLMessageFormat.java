package bridgempp.messageformat;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;

public class HTMLMessageFormat extends MessageFormat {

	public HTMLMessageFormat() {
		parentFormat = MessageFormat.PLAIN_TEXT;
	}
	
	@Override
	public String getName() {
		return "HTML";
	}

	@Override
	public String convertToParent(String message) {
		return message.replaceAll("<.+?>", "");
		
//		EditorKit kit = new HTMLEditorKit();
//		Document document = kit.createDefaultDocument();
//		document.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
//		try {
//			kit.read(new StringReader(message), document, 0);
//			return document.getText(0, document.getLength()).trim();
//		} catch (IOException | BadLocationException e) {
//			Logger.getLogger(HTMLMessageFormat.class.getName()).log(Level.SEVERE, null, e);
//		}
//		return message;
	}

}
