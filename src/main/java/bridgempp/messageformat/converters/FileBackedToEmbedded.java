package bridgempp.messageformat.converters;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import bridgempp.ShadowManager;
import bridgempp.messageformat.Converter;

public class FileBackedToEmbedded extends Converter
{

	public FileBackedToEmbedded()
	{
		super(t -> {
			try
			{
				return IOUtils.toString(new FileInputStream(t), StandardCharsets.UTF_8);
			} catch (IOException e)
			{
				ShadowManager.log(Level.SEVERE, "Failed to read Image from File");
			}
			return "";
		});
	}

}
