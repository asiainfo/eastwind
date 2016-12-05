package eastwind.io.support;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

public class ExpressionUtil {

	private static TemplateEngine ENGINE = new TemplateEngine();

	static {
		StringTemplateResolver resolver = new StringTemplateResolver();
		resolver.setTemplateMode(TemplateMode.TEXT);
	}

	public static String execute(String expression, Object[] args) {
		Context context = new Context();
		context.setVariable("args", args);
		return ENGINE.process("[[${" + expression + "}]]", context).toLowerCase();
	}
	
}