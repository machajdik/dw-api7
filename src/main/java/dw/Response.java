package dw;

import lombok.Getter;

public class Response {

	@Getter
	private String error;
	
	public Response error(String error) {
		this.error = error;
		return this;
	}
	
	protected Response() {
		
	}
	
	

	
	
}
