package ru.ivolkov.asterisk.ami.actions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import ru.ivolkov.asterisk.ami.AsteriskException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionHelper {

	public static String serializeAction(Action action, String lineSeparator) {
		Field[] fields = action.getClass().getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		for (Field field : fields) {
			try {
				String capitalizedName = StringUtils.capitalize(field.getName());
				String getterName = "get" + capitalizedName;
				Method method = action.getClass().getMethod(getterName);
				Object value = method.invoke(action);

				if (value instanceof Map && capitalizedName.equals("Variables")) {
					((Map<?, ?>) value).forEach((key, val) -> sb.append("Variable").append(": ")
							.append(key).append("=").append(val).append(lineSeparator));
				} else if (value != null) {
					sb.append(capitalizedName).append(": ").append(value).append(lineSeparator);
				}

			} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				throw new AsteriskException(e);
			}
		}
		sb.append(lineSeparator);
		return sb.toString();
	}

}
