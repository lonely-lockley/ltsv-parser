# LTSV Parser

**Master:** [![Master build Status](https://travis-ci.com/lonely-lockley/ltsv-parser.svg?branch=master)](https://travis-ci.com/lonely-lockley/ltsv-parser)
**Dev:** [![Dev build Status](https://travis-ci.com/lonely-lockley/ltsv-parser.svg?branch=dev)](https://travis-ci.com/lonely-lockley/ltsv-parser) 
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
<br><br>Parse a FileInputStream:
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
implementation 'com.github.lonely-lockley:ltsv-parser:1.1.0'
```

#### Maven
```xml
<dependency>
    <groupId>com.github.lonely-lockley</groupId>
    <artifactId>ltsv-parser</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Performance compared to ltsv4j
To run performance tests use `./gradlew clean jmh`. Test modes description:
 * xxxMultiStream - parser input as stream with several lines in it (throughput mode)
 * xxxSingleString - parser input as a single message string (throughput mode)
 * xxxMultiStreamAT - parser input as stream with several lines in it (average time mode)
 * xxxSingleStringAT - parser input as a single message string (average time mode)

All testsuites run on MBP late 2019 core i9.
#### JDK 8
```
# JMH version: 1.28
# VM version: JDK 1.8.0_231, Java HotSpot(TM) 64-Bit Server VM, 25.231-b11
# Blackhole mode: full + dont-inline hint
# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations

Benchmark                                    Mode  Cnt          Score        Error  Units
CompareLibs.testLTSV4JParserMultiStream     thrpt   15     729379.086 ± 114016.073  ops/s
CompareLibs.testLTSV4JParserSingleString    thrpt   15     585511.481 ±  44648.473  ops/s
CompareLibs.testLTSV4JParserMultiStreamAT    avgt   15       1207.752 ±    125.387  ns/op
CompareLibs.testLTSV4JParserSingleStringAT   avgt   15       1740.888 ±     81.815  ns/op

CompareLibs.testLtsvParserMultiStream       thrpt   15  160713609.883 ± 997749.139  ops/s
CompareLibs.testLtsvParserSingleString      thrpt   15    1669435.111 ±  73153.612  ops/s
CompareLibs.testLtsvParserMultiStreamAT      avgt   15          6.286 ±      0.088  ns/op
CompareLibs.testLtsvParserSingleStringAT     avgt   15        592.818 ±     12.343  ns/op
```
#### JDK 11
```
# JMH version: 1.28
# VM version: JDK 11.0.9.1, OpenJDK 64-Bit Server VM, 11.0.9.1+11-b1145.77
# Blackhole mode: full + dont-inline hint
# Warmup: 5 iterations, 10 s each
# Measurement: 5 iterations, 10 s each
# Timeout: 10 min per iteration
# Threads: 1 thread, will synchronize iterations

Benchmark                                    Mode  Cnt         Score         Error  Units
CompareLibs.testLTSV4JParserMultiStream     thrpt   15    655398.215 ±   82456.503  ops/s
CompareLibs.testLTSV4JParserSingleString    thrpt   15    228357.689 ±   14201.487  ops/s
CompareLibs.testLTSV4JParserMultiStreamAT    avgt   15      1302.034 ±      30.900  ns/op
CompareLibs.testLTSV4JParserSingleStringAT   avgt   15      4602.262 ±     474.742  ns/op

CompareLibs.testLtsvParserMultiStream       thrpt   15  91420028.647 ± 6547352.545  ops/s
CompareLibs.testLtsvParserSingleString      thrpt   15  13817519.712 ±  941718.683  ops/s
CompareLibs.testLtsvParserMultiStreamAT      avgt   15         9.686 ±       0.418  ns/op
CompareLibs.testLtsvParserSingleStringAT     avgt   15        66.396 ±       2.744  ns/op
```
## Prerequisites

* JDK8+

## License

Licensed under the Apache License, Version 2.0.
