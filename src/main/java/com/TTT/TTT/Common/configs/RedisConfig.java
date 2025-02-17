package com.TTT.TTT.Common.configs;

import com.TTT.TTT.chat.service.RedisPubSubService;
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
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
// 현재 사용하고 있는 레디스 데이터베이스. 0번-리프레시 토큰 데이터 저장 1번-좋아요 데이터 저장

    @Bean(name = "redisConnectionFactory")
    @Qualifier("rtdb")
    public RedisConnectionFactory redisConnectionFactory(){ //레디스 연결을 설정하는 ConnectonFactory객체를 생성하고 반환
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //레디스 서버의 호스트 이름
        configuration.setPort(port); //레디스 서버의 포트 번호
        configuration.setDatabase(0); //레디스에 사용할 데이터베이스 인덱스(refreshToken 관리 인덱스)
        return new LettuceConnectionFactory(configuration); //위에서 설정한 configuration을 기반으로 레디스에 연결할 객체
    }

    @Bean
    @Qualifier("sms")
    public RedisConnectionFactory redisConnectionFactoryForSms(){ //레디스 연결을 설정하는 ConnectonFactory객체를 생성하고 반환
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //레디스 서버의 호스트 이름
        configuration.setPort(port); //레디스 서버의 포트 번호
        configuration.setDatabase(2); //레디스에 사용할 데이터베이스 인덱스
        return new LettuceConnectionFactory(configuration); //위에서 설정한 configuration을 기반으로 레디스에 연결할 객체
    }

    //sms기능 간섭안하는지 체크---------------------------------------------------------------------------------------------
    @Bean
    @Qualifier("sms")
    public StringRedisTemplate stringRedisTemplateforSms(@Qualifier("sms") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }
//    ------------------------

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

    //    연결기본객체
    @Bean("chatPubSubFactory")
    @Qualifier("chatPubSub")
    public RedisConnectionFactory chatPubSubFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//        redis pub/sub에서는 특정 데이터베이스에 의존적이지 않음.
//        configuration.setDatabase(0);
        return new LettuceConnectionFactory(configuration);
    }

    //    publish객체
    @Bean(name = "chattingRedisTemplate")
    @Qualifier("chatPubSub")
//    일반적으로 RedisTemplate<key데이터타입, value데이터타입>을 사용
    public StringRedisTemplate chattingRedisTemplate(@Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory){
        return  new StringRedisTemplate(redisConnectionFactory);
    }

    //    subscribe객체
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("chatPubSub") RedisConnectionFactory redisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("chat"));
        return container;
    }

    //    redis에서 수신된 메시지를 처리하는 객체 생성
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisPubSubService redisPubSubService) {
//        RedisPubSubService의 특정 메서드가 수신된 메시지를 처리할수 있도록 지정
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }



    @Bean
    @Qualifier("likes")
    public RedisConnectionFactory redisConnectionFactoryForLikes(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);
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
