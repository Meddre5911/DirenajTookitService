package direnaj.adapter;

import org.json.JSONException;

public class DirenajInvalidJSONException extends JSONException {

	public DirenajInvalidJSONException(String message) {
		super(message);
	}

	public DirenajInvalidJSONException(Throwable cause) {
		super(cause);
	}

}
