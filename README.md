Bit-lib4j [![Build Status](https://travis-ci.org/devnied/Bit-lib4j.png?branch=master)](https://travis-ci.org/devnied/Bit-lib4j) [![Coverage Status](https://coveralls.io/repos/devnied/Bit-lib4j/badge.png?branch=master)](https://coveralls.io/r/devnied/Bit-lib4j?branch=master)
========

Bit-Lib4j is an useful library to manipulate bits in Java.<br/>
With this library you can read/write data in a byte array with a custom size for Java primitive types.

<b>Current version : 1.4.8</b> 

##Simple API

It is very easy to get started with bit-lib4j:

* Read data from byte array

```java
	byte[] array = new byte[]{0x12,0x25}
	BitUtils bit = new BitUtils(array);
	int res = bit.getNextInteger(4);      // read the first 4 bits to an integer
```

* Create byte array with bit

```java
	BitUtils bit = new BitUtils(8);
	bit.setNextInteger(3,3);			  // set an integer on 3 bits
	bit.setNextInteger(1,5);		      // set one value on 5 bits
	
	// Result
	bit.getData();                        // return Ox61  (0110 0001b)
```

More documentation into the [wiki](https://github.com/devnied/Bit-lib4j/wiki)

## Download

### Maven

```xml
<dependency>
  <groupId>com.github.devnied</groupId>
  <artifactId>bit-lib4j</artifactId>
  <version>1.4.8</version>
</dependency>
```

### JAR

You can download this library on [Maven central](http://search.maven.org/#search%7Cga%7C1%7Cbit-lib4j).

##Dependencies

If you are not using Maven or some other dependency management tool that can understand Maven repositories, the list below is what you need to run bit-lib4j.

**Runtime Dependencies**
* slf4j-api 1.7.5

## Bugs

Please report bugs and feature requests to the GitHub issue tracker.<br/>
Forks and Pull Requests are also welcome.

## Author

**Millau Julien**

+ [http://twitter.com/devnied](http://twitter.com/devnied)
+ [http://github.com/devnied](http://github.com/devnied)


## Copyright and license

Copyright 2013 Millau Julien.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[![Analytics](https://ga-beacon.appspot.com/UA-19411627-5/Bit-lib4j/index)](https://github.com/igrigorik/ga-beacon)
