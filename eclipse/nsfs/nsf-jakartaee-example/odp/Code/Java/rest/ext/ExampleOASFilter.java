package rest.ext;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * Near-NOP OASFilter implementation to test for issue #504
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/504">Issue #504</a>
 */
public class ExampleOASFilter implements OASFilter {
	@Override
	public void filterOpenAPI(OpenAPI openAPI) {
		OASFactory.createInfo();
	}
}
