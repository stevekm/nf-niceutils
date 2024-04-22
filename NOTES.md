# Dev

Set up dev environment using SDK Man

- https://sdkman.io/install
- https://sdkman.io/usage
- https://stackoverflow.com/questions/70751808/how-to-change-the-gradle-version

```bash
# install SDK Man
curl -s "https://get.sdkman.io" | bash

# new terminal session

# install these versions
sdk install java 17.0.10-amzn
sdk install gradle 8.4

# make sure these are set
sdk default java 17.0.10-amzn
sdk default gradle 8.4
```

[Install Nextflow](https://www.nextflow.io/docs/latest/getstarted.html#installation) and leave the `nextflow` executable in the current directory, for convenience

```bash
wget -qO- https://get.nextflow.io | bash
```


compile the plugin and put in into the Nextflow plugin dir

```bash
./gradlew unzipPlugin

# this will place the plugin files at the location:
# ~/.nextflow/plugins/nf-niceutils-0.0.1
```

run the pipeline with the Nextflow binary in the local dir

```bash
./nextflow run examples/test.nf
```

run the test cases

```bash
./gradlew test
```

- if a test failed it will create a report at `build/reports/tests/test/classes/com.nextflow.plugin.PluginTest.html`


## Files

These are the primary files used for dev

- most of the helper methods are in this file: `src/main/groovy/com/nextflow/plugin/ExampleFunctions.groovy`

- the test cases are in this file: `src/test/groovy/com/nextflow/plugin/ExampleFunctionsTest.groovy`

## Release

How to make a Release

- update the Groovy files here

`gradle.properties`

- create a git release

```bash
git tag -a 0.0.1 main
git push --tags origin main
```

- clean up the project to remove stale build artifacts

```bash
./gradlew clean
```

- create a JSON manifest + .zip files for the plugin

```bash
./gradlew jsonPlugin

# creates these files;
# build/plugin/nf-niceutils-0.0.1-meta.json
# build/plugin/nf-niceutils-0.0.1.zip
```

- double check that its right

```bash
$ cat ./build/plugin/nf-niceutils-0.0.1-meta.json
{
    "version": "0.0.1",
    "date": "2024-03-22T14:09:04.655533-04:00",
    "url": "https://github.com/stevekm/nf-niceutils/releases/download/0.0.1/nf-niceutils-0.0.1.zip",
    "requires": ">=22.10.0",
    "sha512sum": "8079463cf9aa1995240c70c9c4e12d5060c1820c266f91858ac0b7cec51779f0ae6ee6ce572a9320ddbdcdd5da2792f6c09445e66913b6e6b112931606b58634"
}

$ sha512sum build/plugin/nf-niceutils-0.0.1.zip
8079463cf9aa1995240c70c9c4e12d5060c1820c266f91858ac0b7cec51779f0ae6ee6ce572a9320ddbdcdd5da2792f6c09445e66913b6e6b112931606b58634  build/plugin/nf-niceutils-0.0.1.zip
```

On [GitHub](https://github.com/stevekm/nf-niceutils/releases) make a new release. Choose the tag that was just created, and attach the .zip file.

Update the [Nextflow Plugins Registry](https://github.com/nextflow-io/plugins/) with the new JSON manifest and submit a PR against it for the update.

- my fork of the registry is here https://github.com/stevekm/nextflow-plugins-registry

To test it out, on another system without the plugin installed (but this repo cloned), run this

```bash
# using my own fork of the registry
NXF_PLUGINS_TEST_REPOSITORY=https://raw.githubusercontent.com/stevekm/nextflow-plugins-registry/main/plugins.json nextflow run examples/test.nf
```

- NOTE: my fork of the registry is here; https://github.com/stevekm/nextflow-plugins-registry

## Extras

>  run for example to generate Nextflow’s artifacts
```bash
./gradlew jsonPlugin
```

if something goes wrong, try this and start again

```bash
./gradlew clean
./gradlew build
```

```bash
rm -rf .gradle
```

## Versions

```bash
$ java --version
openjdk 17.0.10 2024-01-16 LTS
OpenJDK Runtime Environment Corretto-17.0.10.7.1 (build 17.0.10+7-LTS)
OpenJDK 64-Bit Server VM Corretto-17.0.10.7.1 (build 17.0.10+7-LTS, mixed mode, sharing)

$ gradle --version

------------------------------------------------------------
Gradle 8.4
------------------------------------------------------------

Build time:   2023-10-04 20:52:13 UTC
Revision:     e9251e572c9bd1d01e503a0dfdf43aedaeecdc3f

Kotlin:       1.9.10
Groovy:       3.0.17
Ant:          Apache Ant(TM) version 1.10.13 compiled on January 4 2023
JVM:          17.0.10 (Amazon.com Inc. 17.0.10+7-LTS)
OS:           Mac OS X 14.3.1 aarch64
```

# Resources

Nextflow docs
  - https://www.nextflow.io/docs/latest/plugins.html

Slack
  - https://nextflow.slack.com/archives/C02T98A23U7/p1709655400932709

Examples
  - https://github.com/nextflow-io/nf-hello
    - example test cases https://github.com/nextflow-io/nf-hello/blob/master/plugins/nf-hello/src/test/nextflow/hello/TestHelper.groovy
  - https://github.com/nextflow-io/nf-prov
  - https://github.com/bentsherman/nf-boost
  - https://edn-es.github.io/nf-plugin-template/

Gradle notes
  - https://docs.gradle.org/current/userguide/gradle_basics.html
  - https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:adding_wrapper
  - https://docs.gradle.org/current/samples/sample_building_groovy_libraries.html
  - https://www.baeldung.com/ant-maven-gradle
  - https://www.baeldung.com/gradle-sourcecompatiblity-vs-targetcompatibility
  - how to check Groovy versions https://stackoverflow.com/questions/16527384/checking-groovy-version-gradle-is-using

Spock unit test notes
  - https://www.baeldung.com/groovy-spock
