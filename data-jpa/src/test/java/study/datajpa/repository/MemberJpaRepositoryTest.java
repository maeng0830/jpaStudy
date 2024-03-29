package study.datajpa.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Member;

@SpringBootTest
@Transactional // jpa의 모든 데이터 변경은 transaction 안에서 이뤄져야한다.
class MemberJpaRepositoryTest {

	@Autowired
	MemberJpaRepository memberJpaRepository;

//	@Test
//	public void testMember() {
//		Member member = new Member("memberA");
//		Member savedMember = memberJpaRepository.save(member);
//
//		Member findMember = memberJpaRepository.find(savedMember.getId());
//		assertThat(findMember.getId()).isEqualTo(member.getId());
//		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//		assertThat(findMember).isEqualTo(member);
//	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);

		member1.setUsername("member111");

		// 단건 조회 검증
		Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
		Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 데이터 변경 검증
		assertThat(findMember1.getUsername()).isEqualTo("member111");


		//리스트 조회 검증
		List<Member> all = memberJpaRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		//카운트 검증
		long count = memberJpaRepository.count();
		assertThat(count).isEqualTo(2);

		//삭제 검증
		memberJpaRepository.delete(member1);
		memberJpaRepository.delete(member2);
		long deleteCount = memberJpaRepository.count();
		assertThat(deleteCount).isEqualTo(0);
	}

	@Test
	public void findByUsernameAndAgeGreaterThan() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberJpaRepository.save(m1);
		memberJpaRepository.save(m2);

		List<Member> result = memberJpaRepository.findByUsernameAndGreaterThan("AAA", 15);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getAge()).isEqualTo(20);
	}

	@Test
	public void paging() {
		// given
		for (int i = 0; i < 5; i++) {
			memberJpaRepository.save(new Member("member" + i, 10));
		}

		int age = 10;
		int offset = 0;
		int limit = 3;

		// when
		List<Member> members = memberJpaRepository.findByPage(age, offset, limit);
		long totalCount = memberJpaRepository.totalCount(age);

		//then
		assertThat(members.size()).isEqualTo(3);
		assertThat(totalCount).isEqualTo(5);
	}

	@Test
	public void bulkUpdate() {
		for (int i = 0; i < 10; i++) {
			memberJpaRepository.save(new Member("member" + i, 10 + i));
		}

		//when
		int resultCount = memberJpaRepository.bulkAgePlus(15);

		//given
		assertThat(resultCount).isEqualTo(5);
	}
}