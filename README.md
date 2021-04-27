# j9fs

J9fs is a [9p](https://9fans.github.io/plan9port/man/man9/intro.html) file server implemented in Java. The _raison d'etre_ is that i wanted to connect my windows laptop with my Rpi boxes. Of course i could have used [u9fs](https://bitbucket.org/plan9-from-bell-labs/u9fs/src/master/) via WSL, but i wanted to play a bit with modern java and try some new features like records, switch statements, modules, jlink etc.

It works reasonable well for what it does. I start the server on my windows laptop and then connect to Rpi with a reverse ssh tunnel. The server can accept multiple connections but inside it is a single threaded server which is adequate for a single user. Also works well in the opposite direction. Start the server on the Rpi, using the custom JRE that jlink builds, and connect to Rpi from windows WSL using [9pfuse](https://9fans.github.io/plan9port/man/man4/9pfuse.html).

The project needs only the java JDK 16 or later to build. No maven or gradle. This is to keep it simple so that 9p hackers can use it easily.

## Installation
```
> git clone https://github.com/anastasop/j9fs
> cd j9fs
> build
> package
> jre\bin\jre -r d:\tmp\j9fs
```

## Bugs

- no authentication, no authorization.
- no access control. A single user __nobody__ has access everywhere.

## License

Released under the [GPLv3](https://www.gnu.org/licenses/gpl-3.0.en.html).
