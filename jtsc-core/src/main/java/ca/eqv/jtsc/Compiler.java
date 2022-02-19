package ca.eqv.jtsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

public class Compiler {

	private static final String SCRIPT_EXCEPTION_MESSAGE_WHEN_TSC_CANNOT_FIND_SYS = "Cannot read property 'args' of undefined";
	private static final String SCRIPT_EXCEPTION_MESSAGE_WHEN_TSC4_CANNOT_FIND_SYS = "Cannot read property 'tryEnableSourceMapsForHost' of undefined";

	private final GraalJSScriptEngine js;

	private final String tsVersion;
	private final boolean tsVersion4OrNewer;

	public Compiler(final String tsVersion) throws IOException, ScriptException {
		this.tsVersion = tsVersion;
		this.tsVersion4OrNewer = Integer.valueOf(tsVersion.split("\\.", 2)[0]) >= 4;
		this.js = (GraalJSScriptEngine) new ScriptEngineManager().getEngineByName("graal.js");
		final Bindings bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowAllAccess", true);

		loadTsLib("typescript.js");

		try {
			loadTsLib("tsc.js");
		}
		catch (final ScriptException e) {
			String expectedMessage = tsVersion4OrNewer ? SCRIPT_EXCEPTION_MESSAGE_WHEN_TSC4_CANNOT_FIND_SYS : SCRIPT_EXCEPTION_MESSAGE_WHEN_TSC_CANNOT_FIND_SYS;

			if (!e.getMessage().contains(expectedMessage)) {
				throw e;
			}
		}

		bindings.put("jtsc", this);
		bindings.put("jtsc_repackArgs", js.eval("(function () { return arguments; })"));
		loadLocalLib("JVMSystem.js");
	}

	public Integer execute(final String... arguments) throws ScriptException, NoSuchMethodException {
		final Bindings bindings = js.getBindings(ScriptContext.ENGINE_SCOPE);
		final Object ts = bindings.get("ts");
		final Object repackedArguments = js.invokeFunction("jtsc_repackArgs", (Object[]) arguments);

		if (tsVersion4OrNewer) {
			js.invokeMethod(ts, "executeCommandLine", js.eval("ts.sys"), js.eval("ts.noop"), repackedArguments);
		} else {
			js.invokeMethod(ts, "executeCommandLine", repackedArguments);
		}

		final Object exitCode = js.eval("ts.sys.exitCode");
		if (exitCode != null && exitCode instanceof Integer) {
			return (Integer) exitCode;
		} else {
			return null;
		}
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
