package eastwind.io.invocation;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.PropertyUtils;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class PropertyHashCodeGenerator implements HashCodeGenerator {

	private String expression;

	public PropertyHashCodeGenerator(String expression) {
		this.expression = expression;
	}

	@Override
	public int hashCode(InvocationInfo info) {
		Hasher hasher = Hashing.crc32().newHasher();
		// TODO
		try {
			PropertyUtils.getProperty(info.getArgs(), expression);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
