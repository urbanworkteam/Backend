package kr.farmily.api.ai.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "content_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentResult {

    @Id
    @Column(name = "job_id")
    private Long jobId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "card_image_keys", columnDefinition = "text[]", nullable = false)
    private String[] cardImageKeys;

    private String caption;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] hashtags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta;

    public static ContentResult create(Long jobId, String[] cardKeys, String caption,
                                       String[] hashtags, Map<String, Object> meta) {
        ContentResult r = new ContentResult();
        r.jobId = jobId;
        r.cardImageKeys = cardKeys;
        r.caption = caption;
        r.hashtags = hashtags;
        r.meta = meta;
        return r;
    }

    public void editCaption(String caption) {
        if (caption != null) this.caption = caption;
    }

    public void editHashtags(String[] hashtags) {
        if (hashtags != null) this.hashtags = hashtags;
    }
}
