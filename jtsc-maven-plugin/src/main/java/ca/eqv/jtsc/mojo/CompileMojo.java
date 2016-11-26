package ca.eqv.jtsc.mojo;

import ca.eqv.jtsc.Compiler;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.ScriptException;

@SuppressWarnings("unused")
@Mojo(name = "compile", defaultPhase = LifecyclePhase.COMPILE)
class CompileMojo extends AbstractMojo {

	/** Override detected TypeScript version */
	@Parameter
	private String tsVersion;

	/** Command line arguments to tsc */
	@Parameter
	private String[] args;

	private static final String tsVersionPath = "META-INF/maven/org.webjars.npm/typescript/pom.properties";

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (args == null) {
			compile();
		} else {
			compile(args);
		}
	}

	private void compile(final String... args) throws MojoExecutionException {
		try {
			final String version = getTypeScriptVersion();

			final Compiler compiler;
			try {
				getLog().info("Loading TypeScript compiler");
				compiler = new Compiler(version);
			}
			catch (final NullPointerException e) {
				throw new MojoExecutionException("Unable to load TypeScript compiler", e);
			}

			getLog().info("Executing tsc " + Stream.of(args).collect(Collectors.joining(" ")));
			final Integer exitCode = compiler.execute(args);
			if (exitCode == null || exitCode != 0) {
				throw new MojoExecutionException("TypeScript compiler exited with exit code " + exitCode);
			}
		}
		catch (final IOException | ScriptException | NoSuchMethodException e) {
			throw new MojoExecutionException("Error executing TypeScript compiler", e);
		}
	}

	private String getTypeScriptVersion() throws MojoExecutionException {
		if (tsVersion != null) {
			getLog().info("Using configured TypeScript version " + tsVersion);
			return tsVersion;
		}

		final Properties properties = new Properties();
		try (final InputStream pomProperties = getClass().getClassLoader().getResourceAsStream(tsVersionPath)) {
			properties.load(pomProperties);
			final Object detectedTypeScriptVersion = properties.get("version");
			if (detectedTypeScriptVersion != null && detectedTypeScriptVersion instanceof String) {
				getLog().info("Detected TypeScript version " + detectedTypeScriptVersion);
				return (String) detectedTypeScriptVersion;
			}
		}
		catch (final NullPointerException | IOException e) {
			throw new MojoExecutionException("Failed to detect TypeScript version", e);
		}

		throw new MojoExecutionException("Failed to detect TypeScript version");
	}

}
