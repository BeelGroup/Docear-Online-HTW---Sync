# The synchronization daemon for Docear

## Development

### Run tests
* unit tests `sbt test` (excludes all test classes ending with "ITest")
* all tests including integration tests `sbt it:test`
* coverage for all tests `sbt jacoco:cover`
    * the test coverage report is in `target/scala-2.10/jacoco/html/index.html`

#### Integration tests
1. Start play with `sbt -Dconfig.file=conf/syncdaemon.conf compile run`
    * you need to restart play for every test run
2. run `sbt it:test` for the sync daemon on the same machine

### IDE files generation
* Eclipse: `sbt eclipse`
* IntelliJ IDEA: `sbt gen-idea`

### Run
* Copy "config.xml" to %USER_HOME%/.docear/projects/config.xml
* adjust user credentials in config.xml
* adjust rootPath for the existing project to an exisiting folder
* start HTW-Frontend with userMockService
* `sbt run`
* kill with Strg + c or `kill -15 <PID>`
* changing base URL `sbt -Ddaemon.client.baseurl="http://192.168.178.54:9000/api" run`
    * note: maybe a firewall blocks the access

### Configuration for different environments and use cases
* started with `sbt run` the default configuration is application.conf
* started with `sbt -Dconfig.file=developername.conf run` the used configuration is developername.conf
* started with `java -jar <artefact-name>` the used configuration is prod.conf
* change docear home folder: `daemon.docear.home=/tmp/my/docear/home`

### Dependency Injection
* using application.conf or similar files
```
daemon.di {
    full.path.Interface=full.path.Implementation
}
```

### Plugins
* a plugin must extend org.docear.syncdaemon.Plugin
* wire it in application.conf, look at the comment above the key `daemon.plugins`

### Packaging
* `sbt one-jar`
* the artefact is target/sync-daemon-<version>-one-jar.jar
