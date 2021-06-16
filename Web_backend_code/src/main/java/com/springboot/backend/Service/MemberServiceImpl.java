
package com.springboot.backend.Service;

import com.springboot.backend.Entity.Member;
import com.springboot.backend.Repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Override
    public Member getMemberById(String id) {
        return memberRepository.findById(id);
    }

    @Override
    public boolean isIdMatchesPw(String id, String pw) {
        return memberRepository.findById(id).getPw().equals(pw);
    }

    @Override
    public Member getMemberByCarNum(String carNum) {
        return memberRepository.getMemberByCarNum(carNum);
    }

    @Override
    public void updateSeatNum(Member member, Long seatNum) {
        member.setSeatNum(seatNum);
        member.setIsParked(true);
        memberRepository.save(member);
    }

    @Override
    public Member deleteSeatNum(Member member) {
        member.setIsParked(false);
        member.setSeatNum(null);
        member = memberRepository.save(member);
        return member;
    }

    public Member updateMember(Member member){
        return memberRepository.save(member);
    }
}