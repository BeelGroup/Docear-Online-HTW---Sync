# The synchronization daemon for Docear

## Running in dev mode
* Unix: `sbt "run /tmp/folderToWatch"`
* Windows: `sbt "run D:\folderToWatch"`

## Run tests
* `sbt jacoco:cover`
* the test coverage report is in `target/scala-2.10/jacoco/html/index.html`

## IDE files generation
* Eclipse: `sbt eclipse`
* IntelliJ IDEA: `sbt gen-idea`
