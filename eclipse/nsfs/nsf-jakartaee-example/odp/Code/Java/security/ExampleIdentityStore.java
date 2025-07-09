package security;

import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;

@ApplicationScoped
public class ExampleIdentityStore implements IdentityStore {
	public record HeaderCredential(String name) implements Credential {
		public String getName() { return this.name; }
	}
	
	private static final String[] NAMES = {
		"CN=Joe Schmoe/O=SomeOrg"
	};
	
	public CredentialValidationResult validate(HeaderCredential credential) {
		if(Arrays.asList(NAMES).contains(credential.getName())) {
			return new CredentialValidationResult(credential.getName());
		} else {
			return CredentialValidationResult.INVALID_RESULT;
		}
	}
}
