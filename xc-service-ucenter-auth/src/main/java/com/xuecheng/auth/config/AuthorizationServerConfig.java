package com.xuecheng.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;


@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    @Resource
    private DataSource dataSource;  //数据源（从Mysql数据库读取客户端配置）
    /**
     * 读取密钥对（KeyProperties方式会造成循环引用问题）
     * @return
     */
    @Bean("keyProp")
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }
    @Resource(name = "keyProp")
    private KeyProperties keyProperties;
    private final AuthenticationManager authenticationManager;  //认证管理器
    @Resource
    private CustomUserAuthenticationConverter customUserAuthenticationConverter;    //自定义令牌转换器

    /**
     * 令牌转换器
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter( ) {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        //非对称密钥加密
        KeyPair keyPair = new KeyStoreKeyFactory
                (keyProperties.getKeyStore().getLocation(), keyProperties.getKeyStore().getSecret().toCharArray())
                .getKeyPair(keyProperties.getKeyStore().getAlias(), keyProperties.getKeyStore().getPassword().toCharArray());
        converter.setKeyPair(keyPair);
        //配置自定义的令牌转换器
        DefaultAccessTokenConverter accessTokenConverter = (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);
        return converter;
    }

    /**
     * OAuth2 客户端配置
     * @return
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(this.dataSource).clients(new JdbcClientDetailsService(dataSource));
    }

    /**
     * 授权&令牌访问端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager)
                .accessTokenConverter(jwtAccessTokenConverter());

    }

    /**
     * 授权服务安全配置
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();
    }
}

