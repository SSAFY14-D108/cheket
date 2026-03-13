package com.ssafy.cheket.config.s3;

import com.zaxxer.hikari.util.Credentials;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {

    private Credentials credentials = new Credentials();
    private Region region = new Region();
    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Getter
    @Setter
    public static class Region {
        private String staticRegion;
    }

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
    }

}
