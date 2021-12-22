package el;

import java.io.Serializable;

public class FunctionClass implements Serializable {
	private static final long serialVersionUID = 1L;

	public String getFoo() {
		return "I am returned from getFoo()";
	}
	
	public String doFoo(String param) {
		return "I am doFoo(...) and I was told: " + param;
	}
}
