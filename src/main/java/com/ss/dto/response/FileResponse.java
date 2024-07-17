
package com.ss.dto.response;

import com.ss.model.FileModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class FileResponse {
    private UUID id;

    private String name;

    private String url;

    private String urlOriginal;

    public FileResponse(FileModel image) {
        this.id = image.getId();
        this.name = image.getName();
        this.url = image.getUrl();
        this.urlOriginal = image.getUrlOriginal();
    }
}
