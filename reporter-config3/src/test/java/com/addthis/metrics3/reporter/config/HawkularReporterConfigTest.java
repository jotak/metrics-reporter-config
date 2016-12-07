/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.addthis.metrics3.reporter.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.hawkular.metrics.dropwizard.HawkularReporter;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.codahale.metrics.MetricRegistry;

public class HawkularReporterConfigTest {

    private static final Yaml yaml = new Yaml(new Constructor(ReporterConfig.class));

    @Test
    public void testHawkularConfig() throws Exception {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/hawkular.yaml");
        System.out.println(yaml.dump(config));
        assertNotNull(config.getHawkular());
        assertEquals(1, config.getHawkular().size());
        HawkularReporter hawkularReporter = config.getHawkular().get(0).enableAndGet(new MetricRegistry());
        assertEquals(2, hawkularReporter.getGlobalTags().size());
        assertEquals("v1", hawkularReporter.getGlobalTags().get("tag1"));
        assertEquals("v2", hawkularReporter.getGlobalTags().get("tag2"));
        assertEquals(1, hawkularReporter.getPerMetricTags().size());
        assertEquals(2, hawkularReporter.getPerMetricTags().get("sample.metric.rate").size());
        assertEquals("v3", hawkularReporter.getPerMetricTags().get("sample.metric.rate").get("tag3"));
        assertEquals("v4", hawkularReporter.getPerMetricTags().get("sample.metric.rate").get("tag4"));
        assertFalse(hawkularReporter.isEnableAutoTagging());
    }

    @Test
    public void testHawkularMinimalConfig() throws Exception {
        ReporterConfig config = ReporterConfig.loadFromFile("src/test/resources/sample/hawkular-minimal.yaml");
        System.out.println(yaml.dump(config));
        assertNotNull(config.getHawkular());
        assertEquals(1, config.getHawkular().size());
        HawkularReporter hawkularReporter = config.getHawkular().get(0).enableAndGet(new MetricRegistry());
        assertEquals(0, hawkularReporter.getGlobalTags().size());
        assertEquals(0, hawkularReporter.getPerMetricTags().size());
        assertTrue(hawkularReporter.isEnableAutoTagging());
    }

    @Test
    public void usePlainPrefix() {
        String prefix = "test";
        HawkularReporterConfig config = new HawkularReporterConfig();
        config.setPrefix(prefix);
        assertEquals(prefix, config.getPrefix());
    }

    @Test
    public void useHostnamePrefix() throws Exception {
        String prefix = "${host.name}";
        HawkularReporterConfig config = new HawkularReporterConfig();
        config.setPrefix(prefix);
        assertEquals(sanitize(InetAddress.getLocalHost().getHostName()), config.getPrefix());
    }

    // For now this is copied from AbstractHostReporterConfig since the method is private, and it is just needed for
    // a simple test.
    private String sanitize(String string) {
        return string.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
