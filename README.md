Bit-lib4J
========

Bit-Lib4J is an useful library to manipulate bits in Java.
With this library you can read/write data in a byte array with a custom size for Java primitive types.

##Simple API

It is very easy to get started with bit-lib4j:

* Read data from byte tab

```java
	byte[] array = new byte[]{0x12,0x25}
	BitUtils bit = new BitUtils(array);
	int res = bit.getNextInteger(4);      // read the first 4 bits to an integer
```

* Create byte tab with bit

```java
	BitUtils bit = new BitUtils(8);
	bit.setNextInteger(3,3);			  // set and integer on 3 bits
	bit.setNextInteger(1,5);		      // set one value on 4 bits
	
	// Result
	bit.getData();                        // return Ox61  (0110 0001b)
```

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