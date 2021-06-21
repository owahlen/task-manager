plugins {
    id("com.github.node-gradle.node") version "3.1.0"
}

apply(plugin = "base")
apply(plugin = "com.github.node-gradle.node")

// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
    download.set(false)
}

tasks.getByName<Delete>("clean") {
    delete.add("node_modules")
    delete.add("dist")
}

tasks.getByName("yarn_install") {
    inputs.file("package.json")
    outputs.file("yarn.lock")
    outputs.dir("node_modules")
}

tasks.getByName("yarn_build") {
    dependsOn("yarn_install")
    inputs.dir("public")
    inputs.dir("src")
    inputs.file("tsconfig.json")
    outputs.dir("build")
}

val staticWeb by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

artifacts {
    val yarnBuildTask = tasks.getByName("yarn_build")
    add("staticWeb", file("build")) {
        builtBy(yarnBuildTask)
    }
}
