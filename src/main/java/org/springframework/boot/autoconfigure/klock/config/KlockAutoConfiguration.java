package org.springframework.boot.autoconfigure.klock.config;

import io.netty.channel.nio.NioEventLoopGroup;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.klock.core.BusinessKeyProvider;
import org.springframework.boot.autoconfigure.klock.core.KlockAspectHandler;
import org.springframework.boot.autoconfigure.klock.core.LockInfoProvider;
import org.springframework.boot.autoconfigure.klock.lock.LockFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author kl
 * @date 2017/12/29
 * Content :klock自动装配
 */
@Configuration
@ConditionalOnProperty(prefix = KlockConfig.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
//@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(KlockConfig.class)
@Import({KlockAspectHandler.class})
public class KlockAutoConfiguration {

    @Autowired
    private KlockConfig klockConfig;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    RedissonClient redisson() throws Exception {
        Config config = new Config();
        // 集群和单点的配置不一样
        if (klockConfig.getClusterServer() != null) {
            ClusterServersConfig clusterServersConfig = config.useClusterServers();
            clusterServersConfig.addNodeAddress(klockConfig.getClusterServer().toRedisNodeAddresses());
            if (!StringUtils.isEmpty(klockConfig.getPassword())) {
                clusterServersConfig.setPassword(klockConfig.getPassword());
            }
          /*  clusterServersConfig.setMasterConnectionPoolSize(64);//设置对于master节点的连接池中连接数最大为500
            clusterServersConfig.setSlaveConnectionPoolSize(64);//设置对于slave节点的连接池中连接数最大为500
            clusterServersConfig.setIdleConnectionTimeout(10000);//如果当前连接池里的连接数量超过了最小空闲连接数，而同时有连接空闲时间超过了该数值，那么这些连接将会自动被关闭，并从连接池里去掉。时间单位是毫秒。
            clusterServersConfig.setConnectTimeout(30000);//同任何节点建立连接时的等待超时。时间单位是毫秒。
            clusterServersConfig.setTimeout(3000);//等待节点回复命令的时间。该时间从命令发送成功时开始计时。
            clusterServersConfig.setPingTimeout(1000);
            clusterServersConfig.setReconnectionTimeout(3000);//当与某个节点的连接断开时，等待与其重新建立连接的时间间隔。时间单位是毫秒。*/
        } else {
            SingleServerConfig serverConfig = config.useSingleServer();
            serverConfig.setAddress(klockConfig.getAddress());
            if (klockConfig.getDatabase() > 0) {
                serverConfig.setDatabase(klockConfig.getDatabase());
            }
            if (!StringUtils.isEmpty(klockConfig.getPassword())) {
                serverConfig.setPassword(klockConfig.getPassword());
            }
        }
        // 添加解码器
        Codec codec = (Codec) ClassUtils.forName(klockConfig.getCodec(), ClassUtils.getDefaultClassLoader()).newInstance();
        config.setCodec(codec);
        config.setEventLoopGroup(new NioEventLoopGroup());
        return Redisson.create(config);
    }

    @Bean
    public LockInfoProvider lockInfoProvider() {
        return new LockInfoProvider();
    }

    @Bean
    public BusinessKeyProvider businessKeyProvider() {
        return new BusinessKeyProvider();
    }

    @Bean
    public LockFactory lockFactory() {
        return new LockFactory();
    }


    @Bean
    public KlockConfig klockConfig() {
        return new KlockConfig();
    }
}
