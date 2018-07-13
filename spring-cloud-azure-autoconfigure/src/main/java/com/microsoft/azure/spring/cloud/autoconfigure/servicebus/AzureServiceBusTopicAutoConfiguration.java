/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import com.microsoft.azure.servicebus.TopicClient;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryTracker;
import com.microsoft.azure.spring.cloud.context.core.AzureAdmin;
import com.microsoft.azure.spring.integration.servicebus.factory.DefaultServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.factory.ServiceBusTopicClientFactory;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicOperation;
import com.microsoft.azure.spring.integration.servicebus.topic.ServiceBusTopicTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;

/**
 * An auto-configuration for Service Bus topic
 *
 * @author Warren Zhu
 */
@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass(TopicClient.class)
@ConditionalOnProperty("spring.cloud.azure.servicebus.enabled")
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusTopicAutoConfiguration {
    private static final String SERVICE_BUS_TOPIC = "ServiceBusTopic";

    private final AzureServiceBusProperties serviceBusProperties;

    @Autowired(required = false)
    private TelemetryTracker telemetryTracker;

    public AzureServiceBusTopicAutoConfiguration(AzureServiceBusProperties serviceBusProperties) {
        Assert.hasText(serviceBusProperties.getNamespace(), "spring.cloud.azure.servicebus.namespace must be provided");
        this.serviceBusProperties = serviceBusProperties;
    }

    @PostConstruct
    public void triggerTelemetry() {
        TelemetryTracker.triggerEvent(telemetryTracker, SERVICE_BUS_TOPIC);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicClientFactory topicClientFactory(AzureAdmin azureAdmin) {
        return new DefaultServiceBusTopicClientFactory(azureAdmin, serviceBusProperties.getNamespace());
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusTopicOperation topicOperation(ServiceBusTopicClientFactory factory) {
        return new ServiceBusTopicTemplate(factory);
    }
}
