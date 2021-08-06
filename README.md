# LTSV Parser

Master: [![Master build Status](https://travis-ci.com/lonely-lockley/ltsv-parser.svg?branch=master)](https://travis-ci.com/lonely-lockley/ltsv-parser)
Dev:[![Dev build Status](https://travis-ci.com/lonely-lockley/ltsv-parser.svg?branch=dev)](https://travis-ci.com/lonely-lockley/ltsv-parser) 
[![Coverage](https://img.shields.io/codecov/c/gh/lonely-lockley/ltsv-parser)](https://codecov.io/gh/lonely-lockley/ltsv-parser) 
[![Maven Central](https://img.shields.io/maven-central/v/com.github.lonely-lockley/ltsv-parser)](https://search.maven.org/search?q=ltsv-parser)
[![License](https://img.shields.io/github/license/lonely-lockley/ltsv-parser?color=%235b92e5)](http://www.apache.org/licenses/)

A new implementation of a small and fast LTSV parser. Compared to [making/ltsv4j](https://github.com/making/ltsv4j) it:
  * Does not use regular expressions
  * Has flexible configurations
  * Respects quote and escape characters
  * Can parse data in strict and lenient modes
   
**Be careful** - this parser instances are stateful and not synchronized!

## Usage examples
Parser defaults allow to parse LTSV formatted data in strict mode:
```java
LtsvParser parser = LtsvParser.builder().build();
Iterator<Map<String, String>> entries = parser.parse("city:Moscow\tlat:55.7522\tlon:37.6155", 
    StandardCharsets.UTF_8);
if (entries.hasNext()) {
    Map<String, String> result = entries.next();
    System.out.println(result.toString());
}
```
If a data is not fully compatible with LTSV it is still possible to parse that line:
```java
LtsvParser parser = LtsvParser.builder().withQuoteChar('`').withKvDelimiter('=').build();
Iterator<Map<String, String>> entries = parser.parse("city=`New York`\tlat=40.6943\tlon=-73.9249", 
    StandardCharsets.UTF_8);
```
By default parser works in **strict** mode. It will throw a ParseLtsvException if meets a problem in input data. For example a value without a key like this:
```java
LtsvParser parser = LtsvParser.builder().withQuoteChar('`').withKvDelimiter('=').build();
Iterator<Map<String, String>> entries = parser.parse("city=`New York`\t=40.6943\tlon=-73.9249", 
    StandardCharsets.UTF_8);
```
You can enable **lenient** parse mode when building a parser to make it ignore some recoverable errors in input data. In the case above lenient parser returns `null` key for value `40.6943`. Lenient mode tries to do it's best to parse your data, but it is not guaranteed to be parsed correctly. You should test such cases thoroughly.
<br>Parse a FileInputStream:
<br>test.ltsv
```
city=Moscow lat=55.7522 lon=37.6155
city=`New York` lat=40.6943 lon=-73.9249
city=Berlin lat=52.5218 lon=13.4015
city=London lat=51.5000 lon=-0.1167
```
Parser.java
```java
InputStream in = new FileInputStream(new File("test.ltsv"));
LtsvParser parser = LtsvParser.builder().withQuoteChar('`').withKvDelimiter('=').build();
Iterator<Map<String, String>> entries = parser.parse(in);
while (entries.hasNext()) {
    Map<String, String> result = entries.next();
    System.out.println(result.toString());
}
```
## LTSV format description
http://ltsv.org/

## Adding dependency to project
#### Gradle
```groovy
implementation 'com.github.lonely-lockley:ltsv-parser:1.0.0'
```

#### Maven
```xml
<dependency>
    <groupId>com.github.lonely-lockley</groupId>
    <artifactId>ltsv-parser</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Prerequisites

* JDK8+

## License

Licensed under the Apache License, Version 2.0.
