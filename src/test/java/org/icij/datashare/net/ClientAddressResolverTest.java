package org.icij.datashare.net;

import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.fest.assertions.Assertions.assertThat;

public class ClientAddressResolverTest {

    @Test
    public void test_no_trusted_proxies_returns_direct_address() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("");
        InetSocketAddress direct = addr("192.168.1.100");
        assertThat(resolver.resolve(direct, "10.0.0.1")).isEqualTo(direct);
    }

    @Test
    public void test_null_trusted_proxies_returns_direct_address() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver(null);
        InetSocketAddress direct = addr("192.168.1.100");
        assertThat(resolver.resolve(direct, "10.0.0.1")).isEqualTo(direct);
    }

    @Test
    public void test_untrusted_proxy_ignores_xff() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8");
        InetSocketAddress direct = addr("192.168.1.1");
        assertThat(resolver.resolve(direct, "1.2.3.4").getAddress())
                .isEqualTo(InetAddress.getByName("192.168.1.1"));
    }

    @Test
    public void test_trusted_proxy_uses_xff() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8");
        InetSocketAddress direct = addr("10.0.0.1");
        InetSocketAddress resolved = resolver.resolve(direct, "203.0.113.50");
        assertThat(resolved.getAddress()).isEqualTo(InetAddress.getByName("203.0.113.50"));
    }

    @Test
    public void test_trusted_proxy_chain() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8,172.16.0.0/12");
        InetSocketAddress direct = addr("10.0.0.1");
        InetSocketAddress resolved = resolver.resolve(direct, "203.0.113.50, 172.16.0.5, 10.0.0.2");
        assertThat(resolved.getAddress()).isEqualTo(InetAddress.getByName("203.0.113.50"));
    }

    @Test
    public void test_no_xff_header_returns_direct() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8");
        InetSocketAddress direct = addr("10.0.0.1");
        assertThat(resolver.resolve(direct, null)).isEqualTo(direct);
    }

    @Test
    public void test_empty_xff_header_returns_direct() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8");
        InetSocketAddress direct = addr("10.0.0.1");
        assertThat(resolver.resolve(direct, "")).isEqualTo(direct);
    }

    @Test
    public void test_trusted_proxy_with_ipv6_xff() throws UnknownHostException {
        ClientAddressResolver resolver = new ClientAddressResolver("::1/128");
        InetSocketAddress direct = addr("::1");
        InetSocketAddress resolved = resolver.resolve(direct, "2001:db8::1");
        assertThat(resolved.getAddress()).isEqualTo(InetAddress.getByName("2001:db8::1"));
    }

    @Test
    public void test_null_direct_address_returns_null() {
        ClientAddressResolver resolver = new ClientAddressResolver("10.0.0.0/8");
        assertThat(resolver.resolve(null, "1.2.3.4")).isNull();
    }

    private static InetSocketAddress addr(String ip) throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByName(ip), 0);
    }
}
