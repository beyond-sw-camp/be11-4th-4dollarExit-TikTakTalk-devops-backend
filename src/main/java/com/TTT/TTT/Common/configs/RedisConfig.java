package com.TTT.TTT.Common.configs;

import com.TTT.TTT.chat.service.RedisPubSubService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
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
    @Qualifier("sms")
    public StringRedisTemplate stringRedisTemplateforSms(@Qualifier("sms") RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }
//    ------------------------

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

//    채팅화면에서 사용자의 이미지를 뿌려주기 위한 캐싱작업으로 레디스 추가.
    @Bean
    @Qualifier("chatProfileImage")
    public RedisConnectionFactory redisConnectionFactoryForChatImage(){ //레디스 연결을 설정하는 ConnectonFactory객체를 생성하고 반환
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //레디스 서버의 호스트 이름
        configuration.setPort(port); //레디스 서버의 포트 번호
        configuration.setDatabase(3); //레디스에 사용할 데이터베이스 인덱스(refreshToken 관리 인덱스)
        return new LettuceConnectionFactory(configuration); //위에서 설정한 configuration을 기반으로 레디스에 연결할 객체
    }

    @Bean
    @Qualifier("chatProfileImage")
    public RedisTemplate<String, Object> redisTemplateForChatProfileImage() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactoryForChatImage());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // RedisConnectionFactory (Pub/Sub 전용)
    @Bean("pubSubFactory")
    @Qualifier("PubSub")
    public RedisConnectionFactory pubSubFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        return new LettuceConnectionFactory(configuration);
    }

    // RedisTemplate (Pub/Sub 전용)
    @Bean(name = "redisPubSubTemplate")
    @Qualifier("PubSub")
    public StringRedisTemplate redisPubSubTemplate(@Qualifier("pubSubFactory") RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    // RedisMessageListenerContainer: chat, sse-chat 구독
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            @Qualifier("PubSub") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("chatListener") MessageListenerAdapter chatListenerAdapter,
            @Qualifier("sseListener") MessageListenerAdapter sseListenerAdapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // WebSocket 메시지 리스너
        container.addMessageListener(chatListenerAdapter, new PatternTopic("chat"));

        // SSE 메시지 리스너
        container.addMessageListener(sseListenerAdapter, new PatternTopic("sse-chat"));

        return container;
    }

    // WebSocket 메시지 리스너 (chat)
    @Bean
    @Qualifier("chatListener")
    public MessageListenerAdapter chatListenerAdapter(RedisPubSubService redisPubSubService) {
        return new MessageListenerAdapter(redisPubSubService, "onMessage");
    }

    // SSE 메시지 리스너 (sse-chat)
    @Bean
    @Qualifier("sseListener")
    public MessageListenerAdapter sseListenerAdapter(RedisPubSubService redisPubSubService) {
        return new MessageListenerAdapter(redisPubSubService, "onSseMessage");
    }

    //조회수 관련
    @Bean
    @Qualifier("viewCount")
    public RedisConnectionFactory redisConnectionFactoryForClickCount(){ //레디스 연결을 설정하는 ConnectonFactory객체를 생성하고 반환
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host); //레디스 서버의 호스트 이름
        configuration.setPort(port); //레디스 서버의 포트 번호
        configuration.setDatabase(4); //레디스에 사용할 데이터베이스 인덱스(refreshToken 관리 인덱스)
        return new LettuceConnectionFactory(configuration); //위에서 설정한 configuration을 기반으로 레디스에 연결할 객체
    }

    @Bean
    @Qualifier("viewCount")
    public RedisTemplate<String,String> redisTemplateForClickCount(@Qualifier("viewCount") RedisConnectionFactory redisConnectionFactoryForClickCount){
        RedisTemplate<String,String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactoryForClickCount);
        return redisTemplate;
    }


}
