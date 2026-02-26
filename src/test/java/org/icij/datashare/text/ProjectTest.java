package org.icij.datashare.text;

import org.icij.datashare.text.Project;
import org.junit.Test;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;

import static org.fest.assertions.Assertions.assertThat;


public class ProjectTest {

    @Test()
    public void test_constructor_with_only_name_and_default_values() {
        Project project = new Project("local-datashare");
        assertThat(project.name).isEqualTo("local-datashare");
        assertThat(project.label).isEqualTo("local-datashare");
        assertThat(project.allowFromMask).isEqualTo("*.*.*.*");
    }
    @Test()
    public void test_constructor_with_all_values() {
        Project project = new Project(
                "local-datashare",
                "Local Datashare",
                "A sample project",
                Path.of("/vault/local-datashare"),
                "https://icij.org",
                "Jane Doe",
                "ICIJ",
                null,
                "*.*.*.*",
                new Date(),
                new Date());
        assertThat(project.name).isEqualTo("local-datashare");
        assertThat(project.label).isEqualTo("Local Datashare");
        assertThat(project.description).isEqualTo("A sample project");
        assertThat(project.sourceUrl).isEqualTo("https://icij.org");
        assertThat(project.maintainerName).isEqualTo("Jane Doe");
        assertThat(project.publisherName).isEqualTo("ICIJ");
        assertThat(project.allowFromMask).isEqualTo("*.*.*.*");
    }

    @Test
    public void test_is_allowed_with_default_wildcard() throws UnknownHostException {
        Project project = new Project("test");
        assertThat(project.isAllowed(addr("10.0.0.1"))).isTrue();
        assertThat(project.isAllowed(addr("192.168.1.1"))).isTrue();
    }

    @Test
    public void test_is_allowed_with_partial_wildcard() throws UnknownHostException {
        Project project = new Project("test", "10.0.*.*");
        assertThat(project.isAllowed(addr("10.0.1.2"))).isTrue();
        assertThat(project.isAllowed(addr("10.0.255.1"))).isTrue();
        assertThat(project.isAllowed(addr("10.1.0.1"))).isFalse();
        assertThat(project.isAllowed(addr("192.168.1.1"))).isFalse();
    }

    @Test
    public void test_is_allowed_with_cidr() throws UnknownHostException {
        Project project = new Project("test", "192.168.1.0/24");
        assertThat(project.isAllowed(addr("192.168.1.50"))).isTrue();
        assertThat(project.isAllowed(addr("192.168.2.1"))).isFalse();
    }

    @Test
    public void test_is_allowed_with_ipv6_cidr() throws UnknownHostException {
        Project project = new Project("test", "fd00::/8");
        assertThat(project.isAllowed(addr("fd00::1"))).isTrue();
        assertThat(project.isAllowed(addr("fdff::1"))).isTrue();
        assertThat(project.isAllowed(addr("fe00::1"))).isFalse();
    }

    @Test
    public void test_is_allowed_with_mixed_ipv4_and_ipv6() throws UnknownHostException {
        Project project = new Project("test", "10.0.0.0/8,fd00::/8");
        assertThat(project.isAllowed(addr("10.1.2.3"))).isTrue();
        assertThat(project.isAllowed(addr("fd00::1"))).isTrue();
        assertThat(project.isAllowed(addr("192.168.1.1"))).isFalse();
        assertThat(project.isAllowed(addr("fe00::1"))).isFalse();
    }

    @Test
    public void test_static_is_allowed_with_null_project() throws UnknownHostException {
        assertThat(Project.isAllowed(null, addr("10.0.0.1"))).isTrue();
    }

    private static InetSocketAddress addr(String ip) throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByName(ip), 0);
    }
}
