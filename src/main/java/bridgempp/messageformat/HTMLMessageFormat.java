package bridgempp.messageformat;


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
