package ca.eqv.jtsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Compiler {

	private final ScriptEngine js;

	private final String tsVersion;

	public Compiler(final String tsVersion) throws IOException, ScriptException {
		this.tsVersion = tsVersion;

		js = new ScriptEngineManager().getEngineByName("nashorn");
		loadTsc();
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

	/** Assumes that the last line of tsc.js is a call to ts.executeCommandLine. This is probably not a good assumption. */
	private void loadTsc() throws IOException, ScriptException {
		final String tsLibPath = "META-INF/resources/webjars/typescript/" + tsVersion + "/lib/";
		try (final InputStream in = getClass().getClassLoader().getResourceAsStream(tsLibPath + "tsc.js")) {
			final InputStreamReader reader = new InputStreamReader(in);
			final BufferedReader buf = new BufferedReader(reader);
			final String tsc = skipLast(buf.lines()).collect(Collectors.joining("\n"));
			js.eval(tsc);
		}
	}

	private static <T> Stream<T> skipLast(final Stream<T> stream) {
		// http://stackoverflow.com/a/26406722/2391
		final Spliterator<T> split = stream.spliterator();
		return StreamSupport.stream(new Spliterator<T>() {
			final List<T> prev = new ArrayList<>(Collections.singletonList(null));

			@Override
			public boolean tryAdvance(final Consumer<? super T> action) {
				if (prev.get(0) == null) {
					final boolean result = split.tryAdvance(x -> prev.set(0, x));
					if (!result) {
						return false;
					}
				}
				final List<T> next = new ArrayList<>(Collections.singletonList(null));
				final boolean result = split.tryAdvance(x -> next.set(0, x));
				if (!result) {
					return false;
				}
				final T value = prev.get(0);
				prev.set(0, next.get(0));
				action.accept(value);
				return true;
			}

			@Override
			public Spliterator<T> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return split.estimateSize();
			}

			@Override
			public int characteristics() {
				return split.characteristics();
			}
		}, false);
	}

}
