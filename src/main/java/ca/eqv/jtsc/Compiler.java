package ca.eqv.jtsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptContext;
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

		final Bindings bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
		bindings.put("jtsc", this);
		loadLocalLib("JVMSystem.js");
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
		js.eval(readTsLib(filename));
	}

	public String readTsLib(final String filename) throws IOException {
		final String tsLibPath = Paths.get("META-INF", "resources", "webjars", "typescript", tsVersion, "lib", filename).toString();
		try (final InputStream in = Compiler.class.getClassLoader().getResourceAsStream(tsLibPath)) {
			final InputStreamReader reader = new InputStreamReader(in);
			final BufferedReader buf = new BufferedReader(reader);
			final String lib = buf.lines().collect(Collectors.joining("\n"));
			return lib;
		}
	}

}
