
package com.ss.dto.response;

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

}
