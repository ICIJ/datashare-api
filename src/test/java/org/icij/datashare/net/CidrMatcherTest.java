package org.icij.datashare.net;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

public class CidrMatcherTest {

    @Test
    public void test_wildcard_all() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("*.*.*.*");
        assertThat(matcher.matches(InetAddress.getByName("10.0.0.1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("192.168.1.1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("255.255.255.255"))).isTrue();
    }

    @Test
    public void test_wildcard_partial() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("10.0.*.*");
        assertThat(matcher.matches(InetAddress.getByName("10.0.1.2"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("10.0.255.255"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("10.1.0.0"))).isFalse();
        assertThat(matcher.matches(InetAddress.getByName("192.168.1.1"))).isFalse();
    }

    @Test
    public void test_wildcard_single_octet() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("10.20.30.*");
        assertThat(matcher.matches(InetAddress.getByName("10.20.30.1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("10.20.30.255"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("10.20.31.1"))).isFalse();
    }

    @Test
    public void test_wildcard_exact() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("10.20.30.40");
        assertThat(matcher.matches(InetAddress.getByName("10.20.30.40"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("10.20.30.41"))).isFalse();
    }

    @Test
    public void test_cidr_ipv4() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("192.168.1.0/24");
        assertThat(matcher.matches(InetAddress.getByName("192.168.1.1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("192.168.1.255"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("192.168.2.1"))).isFalse();
    }

    @Test
    public void test_cidr_ipv6() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("fd00::/8");
        assertThat(matcher.matches(InetAddress.getByName("fd00::1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("fdff::1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("fe00::1"))).isFalse();
    }

    @Test
    public void test_cidr_ipv6_loopback() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("::1/128");
        assertThat(matcher.matches(InetAddress.getByName("::1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("::2"))).isFalse();
    }

    @Test
    public void test_multiple_cidrs() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("10.0.0.0/8,172.16.0.0/12,::1/128");
        assertThat(matcher.matches(InetAddress.getByName("10.1.2.3"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("172.20.1.1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("::1"))).isTrue();
        assertThat(matcher.matches(InetAddress.getByName("192.168.1.1"))).isFalse();
    }

    @Test
    public void test_ipv6_does_not_match_ipv4_cidr() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("10.0.0.0/8");
        assertThat(matcher.matches(InetAddress.getByName("::1"))).isFalse();
    }

    @Test
    public void test_ipv4_does_not_match_ipv6_cidr() throws UnknownHostException {
        CidrMatcher matcher = new CidrMatcher("::1/128");
        assertThat(matcher.matches(InetAddress.getByName("127.0.0.1"))).isFalse();
    }

    @Test
    public void test_wildcard_to_cidr_conversion() {
        assertThat(CidrMatcher.wildcardToCidr("*.*.*.*")).isEqualTo("0.0.0.0/0");
        assertThat(CidrMatcher.wildcardToCidr("10.*.*.*")).isEqualTo("10.0.0.0/8");
        assertThat(CidrMatcher.wildcardToCidr("10.0.*.*")).isEqualTo("10.0.0.0/16");
        assertThat(CidrMatcher.wildcardToCidr("10.20.30.*")).isEqualTo("10.20.30.0/24");
        assertThat(CidrMatcher.wildcardToCidr("10.20.30.40")).isEqualTo("10.20.30.40/32");
    }

    @Test(expected = CidrMatcher.InvalidCidrException.class)
    public void test_invalid_cidr() {
        new CidrMatcher("not-a-cidr");
    }

    @Test(expected = CidrMatcher.InvalidCidrException.class)
    public void test_invalid_prefix_length() {
        new CidrMatcher("10.0.0.0/33");
    }
}
