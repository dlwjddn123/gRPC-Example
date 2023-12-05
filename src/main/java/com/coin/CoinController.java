package com.coin;

import com.coin.auth.SecurityUtils;
import com.coin.domain.Member;
import com.coin.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/join")
    public void join(@RequestParam String nickname, @RequestParam String password,
                     @RequestParam String loginId, @RequestParam String accountName) {
        coinService.join(loginId, nickname, accountName, password);
    }

    @PostMapping("/signin")
    public void join(@RequestParam String loginId, @RequestParam String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginId, password);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        System.out.println("로그인 성공");
    }

    @PutMapping("/member/modify")
    @Transactional
    public void modify(@RequestParam String nickname, @RequestParam String accountName) {
        Optional<Member> member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId());
        if (member.isEmpty()) {
            System.out.println("로그인이 필요합니다.");
            return;
        }
        Member currentMember = member.get();
        currentMember.modify(nickname, accountName);
        System.out.println("회원 정보가 수정 완료되었습니다.");
    }

    @PatchMapping("/member/change-password")
    @Transactional
    public void changePassword(@RequestParam String password) {
        Optional<Member> member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId());
        if (member.isEmpty()) {
            System.out.println("로그인이 필요합니다.");
            return;
        }
        Member currentMember = member.get();
        currentMember.changePassword(bCryptPasswordEncoder.encode(password));
        System.out.println("비밀번호가 수정 완료되었습니다.");
    }
}
