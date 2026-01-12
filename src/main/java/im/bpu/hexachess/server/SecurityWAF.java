package im.bpu.hexachess.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SecurityWAF {
	private static final List<Predicate<String>> RULES = new ArrayList<>();
	static {
		Pattern sqlPattern = Pattern.compile("(?i)(union|select|insert|delete|update|drop|alter).*(from|into|table|database)");
		RULES.add(input -> sqlPattern.matcher(input).find());

		Pattern xssPattern = Pattern.compile("(?i)(<script|javascript:|onload=|onerror=)");
		RULES.add(input -> xssPattern.matcher(input).find());

		RULES.add(input -> input.length() > 2000);

		RULES.add(input -> input.contains("\0"));
	}
	public static boolean isMalicious(String payload) {
		if (payload == null || payload.isEmpty()) {
			return false;
		}
		return RULES.stream().anyMatch(rule -> rule.test(payload));
	}
}
