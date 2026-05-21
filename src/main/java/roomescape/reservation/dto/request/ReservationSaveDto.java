package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationSaveDto(
        @NotNull(message = "예약자는 필수 항목입니다.")
        Long memberId,

        @NotNull(message = "매장은 필수 항목입니다.")
        Long storeId,

        @NotNull(message = "날짜는 필수 항목입니다.")
        LocalDate date,

        @NotNull(message = "시간 ID는 필수 항목입니다.")
        Long timeId,

        @NotNull(message = "테마 ID는 필수 항목입니다.")
        Long themeId
) {
}
