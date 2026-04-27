package ba.unsa.etf.nbp.travel.config;

import ba.unsa.etf.nbp.travel.storage.BlobStorageProperties;
import ba.unsa.etf.nbp.travel.storage.DisabledDocumentStorage;
import ba.unsa.etf.nbp.travel.storage.DocumentStorage;
import ba.unsa.etf.nbp.travel.storage.S3DocumentStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(BlobStorageProperties.class)
public class BlobStorageConfig {

    @Bean
    @ConditionalOnMissingBean(DocumentStorage.class)
    public DocumentStorage documentStorage(BlobStorageProperties props) {
        if (!props.isEnabled()) {
            return new DisabledDocumentStorage();
        }

        if (props.getProvider() != BlobStorageProperties.Provider.SUPABASE_S3) {
            return new DisabledDocumentStorage();
        }

        if (props.getEndpoint() == null || props.getEndpoint().isBlank()) {
            return new DisabledDocumentStorage();
        }

        if (props.getBucket() == null || props.getBucket().isBlank()) {
            return new DisabledDocumentStorage();
        }

        if (props.getAccessKeyId() == null || props.getAccessKeyId().isBlank()) {
            return new DisabledDocumentStorage();
        }

        if (props.getSecretAccessKey() == null || props.getSecretAccessKey().isBlank()) {
            return new DisabledDocumentStorage();
        }

        var credentials = AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey());

        S3Client s3 = S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(props.getRegion()))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .httpClient(UrlConnectionHttpClient.create())
                .build();

        return new S3DocumentStorage(s3, props.getBucket());
    }
}
