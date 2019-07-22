package com.bdtop.sharding.mybatisplus.autoconfigure.properties;

import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Sharding rule configuration properties.
 *
 * @author Whalley
 */
@ConfigurationProperties(prefix = "sharding.jdbc.config.sharding")
public class ShardingRuleConfigurationProperties extends YamlShardingRuleConfiguration {
}
