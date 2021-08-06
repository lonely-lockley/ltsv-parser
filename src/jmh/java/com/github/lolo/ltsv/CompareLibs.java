package com.github.lolo.ltsv;

import am.ik.ltsv4j.LTSV;
import am.ik.ltsv4j.LTSVParser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@State(value = Scope.Thread)
@Fork(value = 3)
@Threads(1)
public class CompareLibs {

    private String lineSample;
    private InputStream streamSample;

    private LtsvParser ltsv;
    private LTSVParser ltsv4j;

    @Setup(Level.Trial)
    public void initParser() {
        ltsv = LtsvParser.builder().build();
        ltsv4j = LTSV.parser();
    }

    @Setup(Level.Iteration)
    public void initSample() {
        String base = "date:2013-07-26 time:13:26:27 devname:fortigate devid:fg5h1edefad50230 logid:00000000%d type:traffic subtype:forward level:notice vd:root eventtime:%d srcip:127.0.0.1 srcport:5555 srcintf:vlan_1234 srcintfrole:lan dstip:127.0.0.1 dstport:5555 dstintf:vlan_9987 dstintfrole:lan poluuid:%s sessionid:%d proto:6 action:close policyid:12345 policytype:policy service:tcp-100500 dstcountry:reserved srccountry:reserved trandisp:noop duration:2 sentbyte:%d rcvdbyte:%d sentpkt:6 rcvdpkt:5 appcat:unscanned devtype:router_nat_device devcategory:windows_device mastersrcmac:d0:ff:19:ff:ba:a7 srcmac:de:2d:17:84:ae:00 srcserver:0 dstdevtype:windows_pc dstdevcategory:\"windows device\" dstosname:windows_10_2016 masterdstmac:d7:0d:38:2f:d6:83 dstmac:d7:0d:38:2f:d6:83 dstserver:%d";
        Random rng = new Random();
        lineSample = String.format(base, rng.nextInt(99), rng.nextInt(), UUID.randomUUID().toString(), rng.nextInt(), rng.nextInt(), rng.nextInt(), rng.nextInt(9));
        StringBuilder sb = new StringBuilder(1024 * 1024);
        for (int i = 0; i < 1000; i++) {
            sb
                .append(String.format(base, rng.nextInt(99), rng.nextInt(), UUID.randomUUID().toString(), rng.nextInt(), rng.nextInt(), rng.nextInt(), rng.nextInt(9)))
                .append('\n');
        }
        streamSample = new ByteArrayInputStream(sb.toString().getBytes());
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void testLtsvParserSingleString(final Blackhole blackhole) {
        blackhole.consume(ltsv.parse(lineSample, StandardCharsets.UTF_8));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void testLTSV4JParserSingleString(final Blackhole blackhole) {
        blackhole.consume(ltsv4j.parseLine(lineSample));
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void testLtsvParserMultiStream(final Blackhole blackhole) {
        Iterator<Map<String, String>> it = ltsv.parse(streamSample);
        List<Map<String, String>> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        blackhole.consume(list);
    }

    @Benchmark
    @BenchmarkMode({Mode.Throughput})
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void testLTSV4JParserMultiStream(final Blackhole blackhole) {
        List res = ltsv4j.parseLines(streamSample);
        blackhole.consume(res);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testLtsvParserSingleStringAT(final Blackhole blackhole) {
        blackhole.consume(ltsv.parse(lineSample, StandardCharsets.UTF_8));
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testLTSV4JParserSingleStringAT(final Blackhole blackhole) {
        blackhole.consume(ltsv4j.parseLine(lineSample));
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testLtsvParserMultiStreamAT(final Blackhole blackhole) {
        Iterator<Map<String, String>> it = ltsv.parse(streamSample);
        List<Map<String, String>> list = new ArrayList<>();
        it.forEachRemaining(list::add);
        blackhole.consume(list);
    }

    @Benchmark
    @BenchmarkMode({Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void testLTSV4JParserMultiStreamAT(final Blackhole blackhole) {
        List res = ltsv4j.parseLines(streamSample);
        blackhole.consume(res);
    }
}
