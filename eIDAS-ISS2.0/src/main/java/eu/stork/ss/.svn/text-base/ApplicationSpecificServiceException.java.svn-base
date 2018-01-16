package eu.stork.ss;

public class ApplicationSpecificServiceException extends RuntimeException {	
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -5636318385600661553L;

	private String msg;
	private String title;
	
	public ApplicationSpecificServiceException(String title, String msg) {
		this.msg = msg;
		this.title = title;
	}
	
	public String getMessage() {
		return msg;
	}
	
	public String getTitle() {
		return title;
	}
}