package roomescape.closeddate.service;

import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.closeddate.domain.ClosedDate;
import roomescape.closeddate.repository.ClosedDateRepository;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.store.domain.Store;
import roomescape.store.repository.StoreRepository;

@Slf4j
@Service
public class ClosedDateService {
    private final ClosedDateRepository closedDateRepository;
    private final StoreRepository storeRepository;

    public ClosedDateService(ClosedDateRepository closedDateRepository, StoreRepository storeRepository) {
        this.closedDateRepository = closedDateRepository;
        this.storeRepository = storeRepository;
    }

    @Transactional(readOnly = true)
    public List<ClosedDate> findClosedDates() {
        return closedDateRepository.findAll();
    }

    @Transactional
    public ClosedDate register(Long storeId, LocalDate date) {
        if (closedDateRepository.existsByDate(date)) {
            log.warn("Closed date already exists: date={}", date);
            throw new ConflictException("이미 등록된 휴무일입니다.");
        }
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> {
                    log.warn("Store not found: id={}", storeId);
                    return new NotFoundException("존재하지 않는 매장입니다.");
                });

        log.info("Closed date registered: date={}", date);
        return closedDateRepository.save(ClosedDate.create(store, date));
    }

    @Transactional
    public void deregister(Long id) {
        ClosedDate closedDate = findClosedDateOrThrow(id);
        closedDateRepository.delete(id);
        log.info("Closed date deleted: id={}, date={}", closedDate.id(), closedDate.date());
    }

    @Transactional(readOnly = true)
    public List<ClosedDate> findClosedDatesByMemberId(Long memberId) {
        Store store = storeRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NotFoundException("관리 중인 매장이 없습니다."));
        return closedDateRepository.findAllByStoreId(store.id());
    }

    @NonNull
    private ClosedDate findClosedDateOrThrow(Long id) {
        return closedDateRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Closed date not found: id={}", id);
                    return new NotFoundException("존재하지 않는 휴무일입니다.");
                });
    }
}
