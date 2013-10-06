package bmstu.tp.webserver;

public class File {
	public File(byte[] contents, int length, String contentType) {
		this.contents = contents;
		this.length = length;
		this.contentType = contentType;
	}
	
	private byte[] contents;
	private int length;
	private String contentType;
	
	public byte[] getContents() {
		return contents;
	}
	
	public int getLength() {
		return length;
	}
	
	public String getContentType() {
		return contentType;
	}
}
