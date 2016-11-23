package ca.eqv.jtsc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.script.ScriptException;

public class Driver {

	public static void main(final String[] args) throws IOException, ScriptException, NoSuchMethodException {
		final String tsVersion;
		try (final InputStream in = Driver.class.getResourceAsStream("jtsc.properties")) {
			final Properties properties = new Properties();
			properties.load(in);
			tsVersion = properties.getProperty("org.typescriptlang.typescript.version");
		}

		final Compiler compiler = new Compiler(tsVersion);
		compiler.execute(args);
	}

}
