package org.icij.datashare.net;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Matches IP addresses against a list of CIDR blocks.
 * Supports both IPv4 and IPv6 addresses.
 * Also accepts legacy wildcard masks (e.g. {@code 10.0.*.*}) for backward compatibility.
 */
public class CidrMatcher {
    private final List<CidrBlock> blocks;

    /**
     * Creates a matcher from a comma-separated list of CIDR or wildcard entries.
     *
     * @param spec comma-separated CIDR (e.g. {@code "10.0.0.0/8,::1/128"}) or
     *             wildcard (e.g. {@code "10.0.*.*"}) entries
     * @throws InvalidCidrException if any entry cannot be parsed
     */
    public CidrMatcher(String spec) {
        this.blocks = Arrays.stream(spec.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(CidrMatcher::parse)
                .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} if the given address matches any of the configured CIDR blocks.
     */
    public boolean matches(InetAddress addr) {
        for (CidrBlock block : blocks) {
            if (isInSubnet(addr, block)) {
                return true;
            }
        }
        return false;
    }

    private static CidrBlock parse(String entry) {
        if (entry.contains("/")) {
            return parseCidr(entry);
        }
        return parseCidr(wildcardToCidr(entry));
    }

    static String wildcardToCidr(String wildcard) {
        String[] octets = wildcard.split("\\.");
        if (octets.length != 4) {
            throw new InvalidCidrException(wildcard, "wildcard mask must have 4 octets");
        }
        int prefixLength = 0;
        StringBuilder addr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (i > 0) addr.append('.');
            if ("*".equals(octets[i])) {
                addr.append('0');
            } else {
                addr.append(octets[i]);
                prefixLength = (i + 1) * 8;
            }
        }
        return addr + "/" + prefixLength;
    }

    private static CidrBlock parseCidr(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new InvalidCidrException(cidr, "expected format \"address/prefix\"");
        }
        try {
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            int maxPrefix = network.getAddress().length * 8;
            if (prefixLength < 0 || prefixLength > maxPrefix) {
                throw new InvalidCidrException(cidr,
                        String.format("prefix length %d must be between 0 and %d", prefixLength, maxPrefix));
            }
            return new CidrBlock(network, prefixLength);
        } catch (UnknownHostException e) {
            throw new InvalidCidrException(cidr, "cannot resolve network address", e);
        } catch (NumberFormatException e) {
            throw new InvalidCidrException(cidr, "invalid prefix length", e);
        }
    }

    private static boolean isInSubnet(InetAddress addr, CidrBlock block) {
        byte[] addrBytes = addr.getAddress();
        byte[] networkBytes = block.network.getAddress();
        if (addrBytes.length != networkBytes.length) {
            return false;
        }
        int fullBytes = block.prefixLength / 8;
        for (int i = 0; i < fullBytes; i++) {
            if (addrBytes[i] != networkBytes[i]) {
                return false;
            }
        }
        int remainingBits = block.prefixLength % 8;
        if (remainingBits > 0) {
            int mask = (0xFF << (8 - remainingBits)) & 0xFF;
            if ((addrBytes[fullBytes] & mask) != (networkBytes[fullBytes] & mask)) {
                return false;
            }
        }
        return true;
    }

    public static class InvalidCidrException extends RuntimeException {
        public InvalidCidrException(String cidr, String reason) {
            super(String.format("invalid CIDR notation \"%s\": %s", cidr, reason));
        }

        public InvalidCidrException(String cidr, String reason, Throwable cause) {
            super(String.format("invalid CIDR notation \"%s\": %s", cidr, reason), cause);
        }
    }

    private static class CidrBlock {
        final InetAddress network;
        final int prefixLength;

        CidrBlock(InetAddress network, int prefixLength) {
            this.network = network;
            this.prefixLength = prefixLength;
        }
    }
}
