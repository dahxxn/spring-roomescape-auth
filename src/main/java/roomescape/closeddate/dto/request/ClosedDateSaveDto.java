package roomescape.closeddate.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ClosedDateSaveDto(
        @NotNull(message = "매장은 필수 항목입니다.")
        Long storeId,

        @NotNull(message = "날짜는 필수 항목입니다.")
        LocalDate date
) {
}
