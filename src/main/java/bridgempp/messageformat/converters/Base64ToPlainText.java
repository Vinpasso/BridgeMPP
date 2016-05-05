package bridgempp.messageformat.converters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import bridgempp.messageformat.Converter;

public class Base64ToPlainText extends Converter
{
	
	public Base64ToPlainText() {
		super(t -> new String(Base64.getDecoder().decode(t), StandardCharsets.UTF_8));
	}

}
