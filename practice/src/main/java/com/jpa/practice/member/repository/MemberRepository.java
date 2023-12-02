package com.jpa.practice.member.repository;

import com.jpa.practice.member.domain.Member;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MemberRepository {

	private final EntityManager em;

	public void save(Member member) {
		em.persist(member);
	}

	public List<Member> membersWithEager() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	public List<Member> membersWithEagerJoin() {
		return em.createQuery("select m from Member m"
				+ " join m.teamForEager t", Member.class)
				.getResultList();
	}

	public List<Member> membersWithLazy() {
		return em.createQuery("select m from Member m", Member.class)
				.getResultList();
	}

	public List<Member> membersWithLazyJoin() {
		return em.createQuery("select m from Member m"
				+ " join m.teamForLazy t", Member.class)
				.getResultList();
	}

	public List<Member> membersWithFetchJoin() {
		return em.createQuery("select m from Member m"
				+ " join fetch m.teamForLazy", Member.class)
				.getResultList();
	}
}
