package org.icij.datashare.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resolves the real client IP address when behind reverse proxies.
 * <p>
 * If the direct connection comes from a trusted proxy, the {@code X-Forwarded-For}
 * header is walked right-to-left to find the first non-trusted entry.
 * If no trusted proxies are configured, the direct connection address is always returned.
 */
public class ClientAddressResolver {
    private final CidrMatcher trustedProxies;

    /**
     * @param trustedProxiesSpec comma-separated CIDR list of trusted proxy addresses
     *                          (e.g. {@code "127.0.0.0/8,10.0.0.0/8"}), or empty/null to disable
     */
    public ClientAddressResolver(String trustedProxiesSpec) {
        if (trustedProxiesSpec == null || trustedProxiesSpec.trim().isEmpty()) {
            this.trustedProxies = null;
        } else {
            this.trustedProxies = new CidrMatcher(trustedProxiesSpec);
        }
    }

    /**
     * Resolves the real client address from the X-Forwarded-For header chain.
     *
     * @param directAddress   the TCP connection source address
     * @param xForwardedFor   the X-Forwarded-For header value (may be null)
     * @return the resolved client address
     */
    public InetSocketAddress resolve(InetSocketAddress directAddress, String xForwardedFor) {
        if (trustedProxies == null || directAddress == null) {
            return directAddress;
        }
        InetAddress directAddr = directAddress.getAddress();
        if (directAddr == null || !trustedProxies.matches(directAddr)) {
            return directAddress;
        }
        if (xForwardedFor == null || xForwardedFor.trim().isEmpty()) {
            return directAddress;
        }
        List<String> chain = Arrays.stream(xForwardedFor.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        // Walk right-to-left: skip trusted proxies, return first non-trusted IP
        for (int i = chain.size() - 1; i >= 0; i--) {
            try {
                InetAddress addr = InetAddress.getByName(chain.get(i));
                if (!trustedProxies.matches(addr)) {
                    return new InetSocketAddress(addr, 0);
                }
            } catch (UnknownHostException e) {
                return directAddress;
            }
        }
        // All entries are trusted proxies — return the leftmost
        if (!chain.isEmpty()) {
            try {
                return new InetSocketAddress(InetAddress.getByName(chain.get(0)), 0);
            } catch (UnknownHostException e) {
                // fall through
            }
        }
        return directAddress;
    }
}
