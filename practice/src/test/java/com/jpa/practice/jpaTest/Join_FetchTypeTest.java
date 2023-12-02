package com.jpa.practice.jpaTest;

import com.jpa.practice.member.domain.Member;
import com.jpa.practice.member.repository.MemberRepository;
import com.jpa.practice.team.domain.TeamForEager;
import com.jpa.practice.team.domain.TeamForLazy;
import com.jpa.practice.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class Join_FetchTypeTest {

	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private TeamRepository teamRepository;

	@PersistenceContext
	private EntityManager em;

	@Test
	void membersWithEager() {
		TeamForEager teamA = TeamForEager.builder()
				.name("teamA")
				.build();
		TeamForEager teamB = TeamForEager.builder()
				.name("teamB")
				.build();
		teamRepository.saveTE(teamA);
		teamRepository.saveTE(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForEager(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForEager(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.membersWithEager();
		/*
		 * join절 없이, member 엔티티들을 조회한다.
		 *   SELECT m1_0.id ... FROM member m1_0
		 * 즉시 각 member 엔티티들의 teamForEager 엔티티를 각각 조회한다.
		 *   SELECT t1_0.id ... FROM team_for_eager t1_0 WHERE t1_0.id=?
		 *   SELECT t1_0.id ... FROM team_for_eager t1_0 WHERE t1_0.id=?
		 */
	}

	@Test
	void membersWithEagerJoin() {
		TeamForEager teamA = TeamForEager.builder()
				.name("teamA")
				.build();
		TeamForEager teamB = TeamForEager.builder()
				.name("teamB")
				.build();
		teamRepository.saveTE(teamA);
		teamRepository.saveTE(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForEager(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForEager(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.membersWithEagerJoin();
		/*
		 * join 절을 적용하여, member 엔티티들을 조회한다.
		 *   SELECT m1_0.id ... FROM member m1_0 JOIN team_for_eager t1_0 ON t1_0.id=m1_0.team_for_eager_id
		 * 즉시 각 member 엔티티들의 teamForEager 엔티티를 각각 조회한다.
		 *   SELECT t1_0.id ... FROM team_for_eager t1_0 WHERE t1_0.id=?
		 *   SELECT t1_0.id ... FROM team_for_eager t1_0 WHERE t1_0.id=?
		 */
	}

	@Test
	void membersWithLazy() {
		TeamForLazy teamA = TeamForLazy.builder()
				.name("teamA")
				.build();
		TeamForLazy teamB = TeamForLazy.builder()
				.name("teamB")
				.build();
		teamRepository.saveTL(teamA);
		teamRepository.saveTL(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForLazy(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForLazy(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.membersWithLazy();
		/*
		 * join절 없이, member 엔티티들을 조회한다.
		 *   SELECT m1_0.id ... FROM member m1_0
		 */


		for (Member member : members) {
			System.out.println("member.getTeamForLazy().getName() = " + member.getTeamForLazy().getName());
		}
		/*
		 * 각 Member 엔티티의 TeamForLazy 엔티티에 접근할 때마다 각각 조회한다.
		 *   member.getTeamForLazy().getName() = teamA
		 *   SELECT t1_0.id ... FROM team_for_lazy t1_0 WHERE t1_0.id=?
		 *   member.getTeamForLazy().getName() = teamB
		 *   SELECT t1_0.id ... FROM team_for_lazy t1_0 WHERE t1_0.id=?
		 */
	}

	@Test
	void membersWithLazyJoin() {
		TeamForLazy teamA = TeamForLazy.builder()
				.name("teamA")
				.build();
		TeamForLazy teamB = TeamForLazy.builder()
				.name("teamB")
				.build();
		teamRepository.saveTL(teamA);
		teamRepository.saveTL(teamB);

		Member memberA = Member.builder()
				.username("userA")
				.age(10)
				.teamForLazy(teamA)
				.build();
		Member memberB = Member.builder()
				.username("userB")
				.age(20)
				.teamForLazy(teamB)
				.build();
		memberRepository.save(memberA);
		memberRepository.save(memberB);

		em.flush();
		em.clear();

		List<Member> members = memberRepository.membersWithLazyJoin();
		/*
		 * join절을 적용하여, member 엔티티들을 조회한다.
		 *   SELECT m1_0.id ... FROM member m1_0 JOIN team_for_lazy t1_0 ON t1_0.id=m1_0.team_for_lazy_id
		 */


		for (Member member : members) {
			System.out.println("member.getTeamForLazy().getName() = " + member.getTeamForLazy().getName());
		}
		/*
		 * 각 Member 엔티티의 TeamForLazy 엔티티에 접근할 때마다 각각 조회한다.
		 *   member.getTeamForLazy().getName() = teamA
		 *   SELECT t1_0.id ... FROM team_for_lazy t1_0 WHERE t1_0.id=?
		 *   member.getTeamForLazy().getName() = teamB
		 *   SELECT t1_0.id ... FROM team_for_lazy t1_0 WHERE t1_0.id=?
		 */
	}

}
