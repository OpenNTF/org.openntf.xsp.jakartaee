package bean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class ValidationBean {
	private @NotEmpty String foo;
	private @Size(min=3, max=5) String bar;
	
	public String getFoo() {
		return foo;
	}
	public void setFoo(String foo) {
		this.foo = foo;
	}
	public String getBar() {
		return bar;
	}
	public void setBar(String bar) {
		this.bar = bar;
	}
}
