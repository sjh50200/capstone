package com.springboot.backend.Repository;

import com.springboot.backend.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    public Member findById(String id);
    public Member getMemberByCarNum(String carNum);
}
