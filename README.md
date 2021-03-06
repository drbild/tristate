# tristate
[![Build Status](https://travis-ci.org/drbild/tristate.svg?branch=master)](https://travis-ci.org/drbild/tristate)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.davidbild/tristate-core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.davidbild/tristate-core_2.13)
[![Scaladoc](https://www.javadoc.io/badge/org.davidbild/tristate-core_2.13.svg?color=blue&label=Scaladoc)](https://www.javadoc.io/doc/org.davidbild/tristate-core_2.12)

`Tristate[A]` is three-valued variant of `Option[A]` distinguishing between `Present[A](a: A)`, `Absent`, and `Unspecified`.



| Name | Description | Scaladoc |
|------|-------------|----------|
|`tristate-core`|Main library|[![API](https://www.javadoc.io/badge/org.davidbild/tristate-core_2.13.svg?color=blue&label=API)](https://www.javadoc.io/doc/org.davidbild/tristate-core_2.13)|
|`tristate-play`|Play JSON `Reads/Writes`|[![API](https://www.javadoc.io/badge/org.davidbild/tristate-play_2.13.svg?color=blue&label=API)](https://www.javadoc.io/doc/org.davidbild/tristate-play_2.13)|
|`tristate-cats`|Cats Typeclasses|[![API](https://www.javadoc.io/badge/org.davidbild/tristate-cats_2.13.svg?color=blue&label=API)](https://www.javadoc.io/doc/org.davidbild/tristate-cats_2.13)|
|`tristate-scalaz`|Scalaz Typeclasses|[![API](https://www.javadoc.io/badge/org.davidbild/tristate-scalaz_2.13.svg?color=blue&label=API)](https://www.javadoc.io/doc/org.davidbild/tristate-scalaz_2.13)|

## Installation

Add the following to your sbt build:

```scala
libraryDependencies += "org.davidbild" %% "tristate-core" % "0.4.0"
```

To support de/serialization using Play JSON, also add:

```scala
libraryDependencies += "org.davidbild" %% "tristate-play" % "0.4.0"
```

For Scalaz typeclass instances (`Functor`, `Monad`, etc.), also add:
```scala
libraryDependencies += "org.davidbild" %% "tristate-scalaz" % "0.4.0"
```

For Cats typeclass instances (`Functor`, `Monad`, etc.), also add:
```scala
libraryDependencies += "org.davidbild" %% "tristate-cats" % "0.4.0"
```


## Usage

TODO

### Import
```scala
import org.davidbild.tristate.Tristate

// For optional Play JSON support
import org.davidbild.tristate.contrib.play._

// For optional Scalaz typeclass support
import org.davidbild.tristate.contrib.scalaz._

// For optional Cats typeclass support
import org.davidbild.tristate.contrib.cats._
```

## Documents

 - [scaladoc (latest stable release)](http://javadoc-badge.appspot.com/org.davidbild/tristate-core_2.13)

## Contributing

Please submit bugs, questions, suggestions, or (ideally) contributions
as issues and pull requests on Github.

### Maintainers
**David R. Bild** [https://www.davidbild.org](https://www.davidbild.org)


## License
Copyright 2016 David R. Bild

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this work except in compliance with the License. You may obtain a copy of the
License from the LICENSE.txt file or at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed
under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
