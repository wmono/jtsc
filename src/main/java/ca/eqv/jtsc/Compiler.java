package ca.eqv.jtsc;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Compiler {

	private final ScriptEngine js;

	private final String tsVersion;

	public Compiler(final String tsVersion) throws IOException, ScriptException {
		this.tsVersion = tsVersion;
		this.js = new ScriptEngineManager().getEngineByName("nashorn");

		try {
			loadTsLib("tsc.js");
		}
		catch (final ScriptException e) {
			if (!e.getMessage().contains("Cannot read property \"args\" from undefined")) {
				throw e;
			}
		}
		loadLocalLib("JavaSystem.js");
	}

	public void execute() throws ScriptException {
		js.eval("ts.executeCommandLine([])");
	}

	private void loadLocalLib(final String filename) throws IOException, ScriptException {
		try (final InputStream in = getClass().getResourceAsStream(filename)) {
			final InputStreamReader reader = new InputStreamReader(in);
			js.eval(reader);
		}
	}

	private void loadTsLib(final String filename) throws IOException, ScriptException {
		final String tsLibPath = "META-INF/resources/webjars/typescript/" + tsVersion + "/lib/";
		try (final InputStream in = getClass().getClassLoader().getResourceAsStream(tsLibPath + filename)) {
			final InputStreamReader reader = new InputStreamReader(in);
			js.eval(reader);
		}
	}

}
