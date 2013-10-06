package bmstu.tp.webserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileServer {
	private static final Logger LOG = LoggerFactory.getLogger(FileServer.class);
	private static final String FILES_DIR = "./files";
	private static final String DEFAULT_FILE = "index.html";
	private static final Map<String, String> CONTENT_TYPES;
	
	static {
		Map<String, String> types = new HashMap<>();
		types.put(".html", "text/html");
		types.put(".css", "text/css");
		types.put(".js", "application/x-javascript");
		types.put(".jpg", "image/jpeg");
		types.put(".jpeg", "image/jpeg");
		types.put(".png", "image/png");
		types.put(".gif", "image/gif");
		types.put(".swf", "application/x-shockwave-flash");
		CONTENT_TYPES = Collections.unmodifiableMap(types);
	}
	
	public static File getFile(String path, boolean headersOnly) throws FileNotFoundException, ForbiddenException {
		Path completePath;
		if(isDirectory(path)) {
			completePath = Paths.get(FILES_DIR, path, DEFAULT_FILE);
			if(!Files.isRegularFile(completePath)) {
				throw new ForbiddenException();
			}
			LOG.debug("{} is directory", completePath.toString());
		} else {
			completePath = Paths.get(FILES_DIR, path);
			LOG.debug("{} is file", completePath.toString());
		}
		try {
			byte[] contents;
			if(headersOnly) {
				contents = new byte[0];
			} else {
				contents = Files.readAllBytes(completePath);
			}
			String extension = getExtension(completePath.toString());
			return new File(contents, (int)Files.size(completePath), CONTENT_TYPES.get(extension));
		} catch (IOException e) {
			LOG.warn("File not found: {}", completePath);
			throw new FileNotFoundException();
		}
	}
	
	private static boolean isDirectory(String path) {
		return path.charAt(path.length() - 1) == '/';
	}
	
	private static String getExtension(String path) {
		return path.substring(path.lastIndexOf('.'));
	}
}
