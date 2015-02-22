/**
 * 
 */
package direnaj.adapter;

/**
 * 
 *
 */
public class DirenajResponseException extends Exception {

	/**
	 * 
	 */
	public DirenajResponseException() {
		
	}

	/**
	 * @param message
	 */
	public DirenajResponseException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public DirenajResponseException(Throwable cause) {
		super(cause);
		
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DirenajResponseException(String message, Throwable cause) {
		super(message, cause);
		
	}

}
