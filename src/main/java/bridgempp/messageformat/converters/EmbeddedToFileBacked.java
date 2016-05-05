package bridgempp.messageformat.converters;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import bridgempp.ShadowManager;
import bridgempp.messageformat.Converter;

public class EmbeddedToFileBacked extends Converter
{

	public EmbeddedToFileBacked()
	{
		super(t -> {
			try
			{
				File file = File.createTempFile("bridgempp", null);
				file.deleteOnExit();
				IOUtils.write(t, new PrintStream(file), StandardCharsets.UTF_8);
				return file.getAbsolutePath();
			} catch (IOException e)
			{
				ShadowManager.log(Level.SEVERE, "Failed to write Image to File");
			}
			return "";
		});
	}

}
