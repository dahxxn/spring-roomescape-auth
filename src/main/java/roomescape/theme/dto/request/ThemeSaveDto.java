package roomescape.theme.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ThemeSaveDto(
        @NotNull(message = "상점은 필수입니다.")
        Long storeId,

        @NotBlank(message = "테마명은 필수입니다.")
        String name,

        @NotBlank(message = "테마 설명은 필수입니다.")
        String description,

        @NotBlank(message = "썸네일 URL은 필수입니다.")
        String thumbnailUrl
) {
}
