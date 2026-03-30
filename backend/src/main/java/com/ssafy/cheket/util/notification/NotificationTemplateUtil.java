package com.ssafy.cheket.util.notification;

import java.util.Map;

public class NotificationTemplateUtil {

    private NotificationTemplateUtil() {
    }

    public static String replaceVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
