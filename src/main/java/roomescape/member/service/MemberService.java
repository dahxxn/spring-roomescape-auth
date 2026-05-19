package roomescape.member.service;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Transactional
    public Member login(String loginId, String password) {
        Member member = findByLoginId(loginId);
        if (!member.password().equals(password)) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        return member;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public Member join(String name, String loginId, String password) {
        if (memberRepository.findByLoginId(loginId).isPresent()) {
            throw new ConflictException("이미 사용 중인 아이디입니다.");
        }
        return memberRepository.save(Member.create(name, loginId, password));
    }

    @NonNull
    public Member findByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }
}
