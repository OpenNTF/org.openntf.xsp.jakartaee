package el;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.el.ext.ELResolverProvider;

import jakarta.el.ELResolver;

public class CustomELResolverProvider implements ELResolverProvider {

	@Override
	public Collection<ELResolver> provide() {
		return Collections.singleton(new CustomRecordPropertyELResolver());
	}

}
