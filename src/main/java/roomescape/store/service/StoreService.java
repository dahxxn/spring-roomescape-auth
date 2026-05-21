package roomescape.store.service;

import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;

@Service
public class StoreService {
    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public Store findStore(Long id) {
        return findStoreOrThrow(id);
    }

    @Transactional(readOnly = true)
    public Store findStoreByMemberId(Long memberId) {
        return storeRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NotFoundException("관리 중인 매장이 존재하지 않습니다."));
    }

    @NonNull
    private Store findStoreOrThrow(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 매장이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<Store> findAll() {
        return storeRepository.findAll();
    }
}
