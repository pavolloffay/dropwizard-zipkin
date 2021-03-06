/**
 * Copyright 2016 Smoke Turner, LLC.
 *
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
package com.smoketurner.dropwizard.zipkin;

import javax.annotation.Nonnull;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.SpanCollector;
import com.github.kristofa.brave.SpanCollectorMetricsHandler;
import com.github.kristofa.brave.kafka.KafkaSpanCollector;
import com.smoketurner.dropwizard.zipkin.metrics.DropwizardSpanCollectorMetricsHandler;
import io.dropwizard.setup.Environment;

@JsonTypeName("kafka")
public class KafkaZipkinFactory extends AbstractZipkinFactory {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(KafkaZipkinFactory.class);

    @NotEmpty
    private String bootstrapServers;

    @NotEmpty
    private String topic = "zipkin";

    @JsonProperty
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    @JsonProperty
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @JsonProperty
    public String getTopic() {
        return topic;
    }

    @JsonProperty
    public void setTopic(String topic) {
        this.topic = topic;
    }

    /**
     * Build a new {@link Brave} instance for interfacing with Zipkin
     *
     * @param environment
     *            Environment
     * @return Brave instance
     */
    @Override
    public Brave build(@Nonnull final Environment environment) {
        final SpanCollectorMetricsHandler metricsHandler = new DropwizardSpanCollectorMetricsHandler(
                environment.metrics());

        final KafkaSpanCollector.Config kafkaConfig = KafkaSpanCollector.Config
                .builder(bootstrapServers).topic(topic).build();
        final SpanCollector spanCollector = KafkaSpanCollector
                .create(kafkaConfig, metricsHandler);

        LOGGER.info("Sending spans to Kafka topic '{}' at: {}", topic,
                bootstrapServers);

        return buildBrave(environment, spanCollector);
    }
}
