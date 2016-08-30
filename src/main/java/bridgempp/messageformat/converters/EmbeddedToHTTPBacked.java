package bridgempp.messageformat.converters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.messageformat.Converter;

public class EmbeddedToHTTPBacked extends Converter {

	private static String httpLocalPath = "DOES_NOT_EXIST";
	private static String httpRemotePath = "DOES_NOT_EXIST";
	
	public EmbeddedToHTTPBacked() {
		super(t -> {
			Path oldFile = Paths.get("file:/" + t);
			Path file = Paths.get("file:/" + httpLocalPath + "/" + oldFile.getFileName());
			try
			{
				Files.copy(oldFile, file);
			} catch (IOException e)
			{
				ShadowManager.log(Level.WARNING, "Failed to copy into HTTP Folder", e);
				return "FAILURE";
			}
			return httpRemotePath + "/" + file.getFileName();
		});
	}

	
	
}
