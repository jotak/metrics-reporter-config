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

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hawkular.client.dropwizard.HawkularReporter;
import org.hawkular.client.dropwizard.HawkularReporterNullableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.addthis.metrics.reporter.config.AbstractMetricReporterConfig;
import com.codahale.metrics.MetricRegistry;

public class HawkularReporterConfig extends AbstractMetricReporterConfig implements MetricsReporterConfigThree,
        HawkularReporterNullableConfig {

    private static final Logger log = LoggerFactory.getLogger(HawkularReporterConfig.class);
    private HawkularReporter reporter;

    @Valid
    private String uri;
    @NotNull
    private String tenant;
    @Valid
    private String prefix;
    @Valid
    private String bearerToken;
    @Valid
    private Map<String, String> headers;
    @Valid
    private Map<String, String> globalTags;
    @Valid
    private Map<String, Map<String, String>> perMetricTags;
    @Valid
    @Min(0)
    private Long tagsCacheDuration;
    @Valid
    private Boolean autoTagging;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getGlobalTags() {
        return globalTags;
    }

    public void setGlobalTags(Map<String, String> globalTags) {
        this.globalTags = globalTags;
    }

    public Map<String, Map<String, String>> getPerMetricTags() {
        return perMetricTags;
    }

    public void setPerMetricTags(Map<String, Map<String, String>> perMetricTags) {
        this.perMetricTags = perMetricTags;
    }

    public Long getTagsCacheDuration() {
        return tagsCacheDuration;
    }

    public void setTagsCacheDuration(Long tagsCacheDuration) {
        this.tagsCacheDuration = tagsCacheDuration;
    }

    public Boolean getAutoTagging() {
        return autoTagging;
    }

    public void setAutoTagging(Boolean autoTagging) {
        this.autoTagging = autoTagging;
    }

    public HawkularReporter enableAndGet(MetricRegistry registry) {
        if (enable(registry)) {
            return reporter;
        }
        return null;
    }

    @Override
    public void report() {
        if (reporter != null) {
            reporter.report();
        }
    }

    @Override
    public boolean enable(MetricRegistry registry) {
        final String className = "org.hawkular.client.dropwizard.HawkularReporter";
        if (!isClassAvailable(className)) {
            log.error("Tried to enable HawkularReporter, but class {} was not found", className);
            return false;
        }
        try {
            reporter = HawkularReporter.builder(registry, tenant)
                    .withNullableConfig(this)
                    .convertRatesTo(getRealRateunit())
                    .convertDurationsTo(getRealDurationunit())
                    .filter(MetricFilterTransformer.generateFilter(getPredicate()))
                    .build();
            reporter.start(getPeriod(), getRealTimeunit());
        }
        catch (Exception e) {
            log.error("Failed to enable HawkularReporter", e);
            return false;
        }
        return true;
    }
}
