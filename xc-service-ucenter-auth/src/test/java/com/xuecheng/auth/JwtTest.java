package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JwtTest {

    @Test
    public void testCreateJwtToken() {
        String keystore_location = "xczx.jks";
        String keystore_password = "xczxdekey";
        String key_alias = "xczx";
        String key_password = "xczxdekey";
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(keystore_location), keystore_password.toCharArray());
        //密钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(key_alias, key_password.toCharArray());
        //私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //定义JWT Payload信息
        HashMap<String, Object> jwtMap = new HashMap<>();
        jwtMap.put("id", "123");
        jwtMap.put("name", "xnuo");
        jwtMap.put("roles", "r01,r02");
        jwtMap.put("ext", "1");
        //生成JWT令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(jwtMap), new RsaSigner(aPrivate));
        System.out.println("token=" + jwt.getEncoded());
    }

    @Test
    public void testVerifyJwtToken() {
        //String jwtToken="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHQiOiIxIiwicm9sZXMiOiJyMDEscjAyIiwibmFtZSI6InhudW8iLCJpZCI6IjEyMyJ9.A4eECTzzwf8XPs_Fyhyc1sfKcQlkzJ158OXSH7CHr6y6HhNPo_zfqsGN_9qKqVeUTAnmDWaC_3HF7YlcaaiJSXEQYeFQhuofzncAQRultQUtmHz7DhCvZZzXe8asQ-DhYnD57NWlllhhsKxsPKSTdLTa0WOXYFdeLbL8K0iEnob7Ugozjvf5UKdKCLn1VNJ-mZsAwjYmenLqaOtjoAd_KKmlWzYm59laGElIoKLwKSane_nQAHX19nuNhWoRk8M5uL1WrmA674rQy5q3q0F8YPsZanpdSdn-hy7hYpH7jbxc4moQ8lxv2tNU0xs4eH58nLYbxjLHBDj7GFzhGkUiBw";
        String jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOm51bGwsInVzZXJwaWMiOm51bGwsInVzZXJfbmFtZSI6ImFkbWluIiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJ1dHlwZSI6IjEwMTAwMyIsImlkIjoiNDgiLCJleHAiOjE3MTEzOTc4ODEsImF1dGhvcml0aWVzIjpbInhjX3N5c21hbmFnZXJfZG9jIiwieGNfc3lzbWFuYWdlcl91c2VyIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9iYXNlIiwieGNfc3lzbWFuYWdlcl91c2VyX3ZpZXciLCJ4Y19zeXNtYW5hZ2VyIiwieGNfc3lzbWFuYWdlcl9sb2ciLCJ4Y19zeXNtYW5hZ2VyX3VzZXJfZWRpdCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2UiLCJ4Y19zeXNtYW5hZ2VyX3VzZXJfYWRkIiwieGNfc3lzbWFuYWdlcl91c2VyX2RlbGV0ZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjgzOGNlYmM5LTBkNzctNDM0YS1iNzUxLTUzMDJiYWMxY2RkYiIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.jC_sCAE9ros3iKEOSAcYhdD04UBhWHYpSFTIp1tlDzGSSIJI1sQqBfwA1RmIjyNAZVIDNuhiZOOFPt4XKyidaO6ssuPbecr5bUzpezT8-pJtaFTgkDQe9v1VZOrBdyB67YwYXpWhgt3KnzB3CLr6GSz0kgzUmObXNrqr_LTAAK9HQMlnsflqFxIYqNFoUI7Yt50yziywdWoKPXXY2v5a-Pt6VvIAVIkxB2cOgzM7jTNL9qO2MyMVdriTSvZve-pSLSqa8V5YDVewwPwxEOADVkJpkNnSU4-NUYX-VdPoKJl5rDwA9S8WYddkWZe3vi8D2STudKvZKeihEZAdE_bW4w";
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkClqBwBYD+LnE5vaskaAcrnSz4X6Zsk17bJnSc5WJIwR6f3I7UkFS2PHy9p80xDVAxcq6vYzJjZCtG9WFyDC5ABONGBsHMZG8yg2n6KAyG33k6faVtdtLcdTVIDunC+FiDPZcvhW31yn5fdVgRvNdoApmqbpXPBJjHF/xxkOxhn5JbqROZKhBEJKNYGwRhCzQEBr2N8X1WJ5B5Nj9DLtRfqlIdNiGHmdAa+5sbiY5CrqRW4ZJr69PsjgFg28g3CCX8++y/AwjYNJ4bTvMM0U0IM2Q39EPcK8gEjlz5NKFIgLKFUP48au1zcyjlKkUQOsWi1V5DTNjeXbfjjwNZL7uwIDAQAB-----END PUBLIC KEY-----";
        //校验JWT令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtToken, new RsaVerifier(publickey));
        //获取JWT Payload信息
        String claims = jwt.getClaims();
        System.out.println(claims);

    }
    
}
