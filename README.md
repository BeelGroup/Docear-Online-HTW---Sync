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

## Dependency Injection
* using application.conf or similar files
```
daemon.di {
    full.path.Interface=full.path.Implementation
}
```

## Plugins
* a plugin must extend org.docear.syncdaemon.Plugin
* wire it in application.conf, look at the comment above the key `daemon.plugins`