package com.springboot.backend.Service;

import com.springboot.backend.Entity.Member;

public interface MemberService {

    public Member getMemberById(String id);
    public boolean isIdMatchesPw(String id, String pw);
    public Member getMemberByCarNum(String carNum);
    public Member deleteSeatNum(Member member);
    public void updateSeatNum(Member member, Long seatNum);
    public Member updateMember(Member member);
}
