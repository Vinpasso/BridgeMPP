package bridgempp.message;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class MIMEProperty extends MimeType implements MessageProperty {
	
	public class MIMEType {
		private MIMEPart[] elements;
		
		public MIMEType(MIMEPart... elements)
		{
			this.elements = elements;
		}
		
		public void match(MIMEType other)
		{
			int minLength = Math.min(elements.length, other.elements.length);
			for(int i = 0; i < minLength)
			{
				if()
			}
			return true;
		}
	}
	
	public enum MIMEPart
	{
		TEXT,
		MEDIA,
		IMAGE,
		PLAIN,
		HTML,
		XHTML,
		
	}
	
	
	public static MIMEProperty text = new MIMEProperty("text/*");
	
	
	public MIMEProperty(String data) throws MimeTypeParseException
	{
		super(data);
	}
	
	public boolean equals(Object other)
	{
		if(other instanceof MIMEProperty)
		{
			return match((MIMEProperty) other);
		}
		return false;
	}
	
}
