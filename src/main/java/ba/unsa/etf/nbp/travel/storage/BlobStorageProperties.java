package ba.unsa.etf.nbp.travel.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.blob")
public class BlobStorageProperties {
    private boolean enabled = false;
    private Provider provider = Provider.SUPABASE_S3;
    private String endpoint = "";
    private String region = "us-east-1";
    private String bucket = "nbp-documents";
    private String accessKeyId = "";
    private String secretAccessKey = "";

    public enum Provider {
        SUPABASE_S3
    }
}
