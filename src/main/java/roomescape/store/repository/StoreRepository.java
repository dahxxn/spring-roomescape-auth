package roomescape.store.repository;

import java.util.List;
import java.util.Optional;
import roomescape.store.domain.Store;

public interface StoreRepository {
    Optional<Store> findById(Long id);
    Optional<Store> findByMemberId(Long memberId);
}
