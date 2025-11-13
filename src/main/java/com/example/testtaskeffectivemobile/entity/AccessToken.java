package com.example.testtaskeffectivemobile.entity;


import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "AccessToken")
@Builder
public class AccessToken implements Serializable {

    @Id
    @Indexed
    private String id;
    private List<String> accessToken;
}
