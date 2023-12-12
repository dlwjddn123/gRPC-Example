package com.coin.controller;

import com.coin.auth.SecurityUtils;
import com.coin.domain.Member;
import com.coin.repository.MemberRepository;
import com.coin.service.CoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final CoinService coinService;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/join")
    public String join(@RequestParam String nickname, @RequestParam String password,
                     @RequestParam String loginId, @RequestParam String accountName) {
        return coinService.join(loginId, nickname, accountName, password);
    }

    @PostMapping("/signin")
    public String join(@RequestParam String loginId, @RequestParam String password) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(loginId, password);
        Optional<Member> member = memberRepository.findByLoginId(loginId);
        if (member.isEmpty()) {
            return "[ERROR] 해당 아이디는 존재하지 않습니다.";
        }
        if (!bCryptPasswordEncoder.matches(password, member.get().getPassword())) {
            return "[ERROR] 비밀번호가 올바르지 않습니다";
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return "로그인 성공";
    }

    @GetMapping("/getSessionId")
    public String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return session.getId();
    }

    @PutMapping("/member/modify")
    @Transactional
    public String modify(@RequestParam String nickname, @RequestParam String accountName) {
        Optional<Member> member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId());
        if (member.isEmpty()) {
            return "[ERROR] 로그인이 필요합니다.";
        }
        Member currentMember = member.get();
        Member.Account.of(accountName);
        return currentMember.modify(nickname, accountName);
    }

    @PatchMapping("/member/change-password")
    @Transactional
    public String changePassword(@RequestParam String password) {
        Optional<Member> member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId());
        if (member.isEmpty()) {
            return "[ERROR] 로그인이 필요합니다.";
        }

        if (password == null || password.isEmpty()) {
            return "[ERROR] 새로운 비밀번호가 올바르지 않습니다.";
        }
        Member currentMember = member.get();
        currentMember.changePassword(bCryptPasswordEncoder.encode(password));
        return "비밀번호가 수정 완료되었습니다.";
    }

    @GetMapping("/member")
    public String getProfile() {
        Optional<Member> member = memberRepository.findByLoginId(SecurityUtils.getLoggedUserLoginId());
        if (member.isEmpty()) {
            return "[ERROR] 로그인이 필요합니다.";
        }
        StringBuilder result = new StringBuilder();
        result.append("------------------------------------------------------\n\n");
        result.append("닉네임 : " + member.get().getNickname() + "\n");
        result.append("계좌 정보 : " + member.get().getAccount().toString() + "\n\n");
        result.append("------------------------------------------------------\n\n");
        return result.toString();
    }

}
