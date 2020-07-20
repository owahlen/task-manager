plugins {
	id("com.github.node-gradle.node") version "2.2.4"
}

group = "org.example"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

apply(plugin = "base")
apply(plugin = "com.github.node-gradle.node")


node {
	// node-gradle configuration:
	// https://github.com/srs/gradle-node-plugin/blob/master/docs/node.md
	// Task name pattern:
	// ../gradlew yarn_<command> Executes a yarn command.

	// Version of node to download and install (only used if download is true)
	// It will be unpacked in the workDir
	version = "14.3.0"

	// Version of Yarn to use
	// Any Yarn task first installs Yarn in the yarnWorkDir
	// It uses the specified version if defined and the latest version otherwise (by default)
	yarnVersion = "1.22.4"

	// Whether to download and install a specific Node.js version or not
	// If false, it will use the globally installed Node.js
	// If true, it will download node using above parameters
	// Note that npm is bundled with Node.js
	download = true
}
