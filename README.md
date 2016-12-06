# JVM Wrapper for the TypeScript Compiler

This project is
 - a wrapper to allow `tsc` to run on the JVM, plus
 - a Maven plugin to compile TypeScript as part of a build

## Warning

This project is currently in a proof-of-concept stage and is not
suitable for use by end users.

## Why?

There are a variety of reasons that a development team can't require
Node.js and NPM for their build process, including company policy,
regulatory and audit compliance, and inertia. While individual
developers may be able to work around these roadblocks, a requirement
to install new software on the build server is more likely to remain
a problem.

## How to use

- Build and install the plugin into your local repository:
```
	$ mvn -am -pl jtsc-maven-plugin install
```
- Configure the plugin in your project:
```xml
	<plugin>
		<groupId>ca.eqv.jtsc</groupId>
		<artifactId>jtsc-maven-plugin</artifactId>
		<version>0.1.0.BUILD-SNAPSHOT</version>
		<executions>
			<execution>
				<goals>
					<goal>compile</goal>
				</goals>
			</execution>
		</executions>
	</plugin>
```
- Place a `tsconfig.json` in your project root (next to `pom.xml`)
- Compile your project.

### Try the self-contained executable

- Build the executable:
```
	$ mvn -am -pl jtsc-executable package
```
- Run it:
```
	$ java -jar jtsc-executable/target/jtsc-executable-*-jar-with-dependencies.jar --help
```

## See also

If installing/executing Node.js is a possibility, consider other
plugins such as
[frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin)
and
[grunt-maven-plugin](https://github.com/allegro/grunt-maven-plugin).

This project is intended to replace
[tsc-maven-plugin](https://github.com/wmono/tsc-maven-plugin)
which is only capable of running tsc versions older than 1.5.

## Credits

Based on work and discussion in
[TypeScript #8565](https://github.com/Microsoft/TypeScript/pull/8565)
