package com.weixin.util;

import java.io.IOException;
import java.util.ResourceBundle;

public class Config {

	private static ResourceBundle bundle;
	static {
		bundle = ResourceBundle.getBundle("config");
	}
	
	public static String getString(String key) {
		return bundle.getString(key);
	}

	@SuppressWarnings("static-access")
	public static void reload() {
		bundle.clearCache();
		bundle = ResourceBundle.getBundle("config", new ResourceBundle.Control() {
			public ResourceBundle newBundle(String baseName, java.util.Locale locale, String format, ClassLoader loader,
					boolean reload) throws IllegalAccessException, InstantiationException, IOException {
				return super.newBundle(baseName, locale, format, loader, true);
			}
		});
	}

}
