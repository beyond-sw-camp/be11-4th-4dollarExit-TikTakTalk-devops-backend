package com.TTT.TTT.Common.configs;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
// 현재 사용하고 있는 레디스 데이터베이스. 1번-리프레시 토큰 데이터 저장 2번-좋아요 데이터 저장

    @Bean
    @Qualifier("rtdb")
    public RedisConnectionFactory redisConnectionFactory(){ //레디스 연결을 설정하는 ConnectonFactory객체를 생성하고 반환
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //레디스 서버의 호스트 이름
        configuration.setPort(port); //레디스 서버의 포트 번호
        configuration.setDatabase(1); //레디스에 사용할 데이터베이스 인덱스(수업에 0번 데이터베이스를 이용하여 데이터 겹칠 것 같아 1번 사용했습니다)
        return new LettuceConnectionFactory(configuration); //위에서 설정한 configuration을 기반으로 레디스에 연결할 객체
        }

    @Bean(name = "redisTemplate")
    @Qualifier("rtdb")
//    RedisTemplate는 레디스DB와의 데이터 입출력작업을 간편하게 도와주는 스프링 클래스
    public RedisTemplate<String,Object> redisTemplate(@Qualifier("rtdb")RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer()); //레디스는 데이터를 바이트배열로 저장하기 때문에 데이터를 사람이 읽을 수 있는 형태로 변환(직렬화)해야함. 이것을 String으로 하겠다고 선언
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//sms기능 간섭안하는지 체크---------------------------------------------------------------------------------------------
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }
//    ----------------------------------------------------------------------------------------------------------

    @Bean
    @Qualifier("likes")
    public RedisConnectionFactory redisConnectionFactoryForLikes(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(2);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("likes")
    public RedisTemplate<String,String> redisTemplateforLikes(@Qualifier("likes") RedisConnectionFactory redisConnectionFactoryForLikes){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactoryForLikes);
        return redisTemplate;
    }

}
