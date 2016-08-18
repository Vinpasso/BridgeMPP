package bridgempp.messageformat.converters;

import java.io.IOException;
import java.nio.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import bridgempp.messageformat.Converter;

public class EmbeddedToHTTPBacked extends Converter {

	private static String httpLocalPath = "DOES_NOT_EXIST";
	private static String httpRemotePath = "DOES_NOT_EXIST";
	
	public EmbeddedToHTTPBacked() {
		super(t -> {
			Path oldFile = Paths.get("file:/" + t);
			Path file = Paths.get("file:/" + httpLocalPath + "/" + oldFile.getFileName());
			Files.copy(oldFile, file);
			return httpRemotePath + "/" + file.getFileName();
		});
	}

	
	
}
