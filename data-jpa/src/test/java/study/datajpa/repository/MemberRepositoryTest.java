package study.datajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;
	@Autowired
	TeamRepository teamRepository;
	@PersistenceContext
	EntityManager em;

	@Test
	public void testMember() {
		System.out.println("memberRepository.getClass() = " + memberRepository.getClass());

		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		Member findMember = memberRepository.findById(savedMember.getId()).get();
		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		member1.setUsername("member111");

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 데이터 변경 검증
		assertThat(findMember1.getUsername()).isEqualTo("member111");


		//리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		//카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		//삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);
		long deleteCount = memberRepository.count();
		assertThat(deleteCount).isEqualTo(0);
	}

	@Test
	public void findByUsernameAndGreaterThan() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
		assertThat(result.size()).isEqualTo(1);
		assertThat(result.get(0).getAge()).isEqualTo(20);
	}

	@Test
	public void findHelloBy() {
		List<Member> helloBy = memberRepository.findTop3By();
	}

	@Test
	public void testQuery() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findUser("AAA", 10);
		assertThat(result.get(0)).isEqualTo(m1);
	}

	@Test
	public void findUsernameList() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<String> result = memberRepository.findUsernameList();
		assertThat(result.size()).isEqualTo(2);
	}

	@Test
	public void findMemberDto() {
		Team team = new Team("teamA");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		memberRepository.save(m1);
		m1.changeTeam(team);

		List<MemberDto> result = memberRepository.findMemberDto();
		assertThat(result.get(0)).isInstanceOf(MemberDto.class);

		System.out.println("result = " + result.get(0));
	}

	@Test
	public void findByNames() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		assertThat(result.size()).isEqualTo(2);
	}

	@Test
	public void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);
		memberRepository.save(m1);
		memberRepository.save(m2);

		List<Member> list = memberRepository.findListByUsername("AAA");
		Member member = memberRepository.findMemberByUsername("AAA");
		Optional<Member> optionalMember = memberRepository.findOptionalByUsername("AAA");
	}

	/// paging

	@Test
	public void pagingPage() {
		// given
		for (int i = 0; i < 5; i++) {
			memberRepository.save(new Member("member" + i, 10));
		}

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));

		// when
		Page<Member> page = memberRepository.findPageByAge(age, pageRequest);

		Page<MemberDto> toMap = page.map(
				m -> new MemberDto(m.getId(), m.getUsername(), null));

		//then
		assertThat(toMap.getContent().size()).isEqualTo(3); // 현재 페이지의 데이터는 몇개인가?
		assertThat(toMap.getTotalElements()).isEqualTo(5); // 총 페이지의 데이터는 몇개인가?
		assertThat(toMap.getNumber()).isEqualTo(0); // 현재 페이지는 몇번째 페이지인가?
		assertThat(toMap.getTotalPages()).isEqualTo(2); // 총 페이지는 몇개인가?
		assertThat(toMap.isFirst()).isTrue(); // 현재 페이지는 첫번째 페이지인가?
		assertThat(toMap.hasNext()).isTrue(); // 다음 페이지가 있는가?
	}

	@Test
	public void pagingSlice() {
		// given
		for (int i = 0; i < 5; i++) {
			memberRepository.save(new Member("member" + i, 10));
		}

		int age = 10;
		// Slice를 사용할 경우, size + 1만큼 요청한다. count 쿼리도 나가지 않는다(필요가 없다).
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Direction.DESC, "username"));

		// when
		Slice<Member> slice = memberRepository.findSliceByAge(age, pageRequest);

		Slice<MemberDto> toMap = slice.map(
				m -> new MemberDto(m.getId(), m.getUsername(), null));

		//then
		assertThat(toMap.getContent().size()).isEqualTo(3); // 현재 페이지의 데이터는 몇개인가?
		assertThat(toMap.getNumber()).isEqualTo(0); // 현재 페이지는 몇번째 페이지인가?
		assertThat(toMap.isFirst()).isTrue(); // 현재 페이지는 첫번째 페이지인가?
		assertThat(toMap.hasNext()).isTrue(); // 다음 페이지가 있는가?
	}

	@Test
	public void bulkUpdate() {
		for (int i = 0; i < 10; i++) {
			memberRepository.save(new Member("member" + i, 10 + i));
		}

		//when
		int resultCount = memberRepository.bulkAgePlus(15);

		// 벌크 연산은 영속성 컨텍스트를 거치지 않고 바로 DB에 적용된다. 영속성 컨텍스트에 있는 영속성 엔티티에는 반영이 안된다.
		Member preMember5 = memberRepository.findMemberByUsername("member5");
		System.out.println("preMember5.getAge() = " + preMember5.getAge()); // 15

		// 벌크 연산 후에는 영속성 컨텍스트를 정리하고 다시 불러오는 것이 좋다. 특히 벌크 연산 뒤에 추가 로직이 있는 경우..
		em.flush();
		em.clear(); // @Modifying의 옵션으로 해결 가능!
		Member postMember5 = memberRepository.findMemberByUsername("member5");
		System.out.println("member15.getAge() = " + postMember5.getAge()); // 16

		//given
		assertThat(resultCount).isEqualTo(5);
	}

	///// fetch join

	@Test
	public void findMemberLazy() {
		//given
		//member1 -> teamA
		//member2 -> teamB

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		//when
		//select Member 쿼리 실행, 1
		List<Member> members = memberRepository.findAll();

		for (Member member : members) {
			System.out.println("member.getUsername() = " + member.getUsername());
			// 실제 Team 객체를 사용하기 전에는 proxy 객체
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
			// 실제 Team 객체를 사용할 때 각각 Select Team 쿼리 실행, N
			System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
		}

		// -> N + 1 문제 발생
	}

	@Test
	public void findMemberFetchJoinAndEntityGraph() {
		//given
		//member1 -> teamA
		//member2 -> teamB

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		teamRepository.save(teamA);
		teamRepository.save(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 10, teamB);
		memberRepository.save(member1);
		memberRepository.save(member2);

		em.flush();
		em.clear();

		//when
		// JPQL을 통해 직접 fetch join 쿼리 작성
		//select Member, Team 쿼리 실행 1
		List<Member> memberFetchJoin = memberRepository.findMemberFetchJoin();

		for (Member member : memberFetchJoin) {
			System.out.println("member.getUsername() = " + member.getUsername());
			// 실제 Team 객체
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
			System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
		}

		// -> N + 1 문제 해결

		// JPQL에 fetch join 쿼리를 작성하지 않고, @EntityGraph를 적용하여 fetch join 할 수 있다.
		//select Member, Team 쿼리 실행 1
		em.flush();
		em.clear();
		List<Member> memberEntityGraph = memberRepository.findMemberEntityGraph();

		for (Member member : memberEntityGraph) {
			System.out.println("member.getUsername() = " + member.getUsername());
			// 실제 Team 객체
			System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
			System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
		}

		// -> N + 1 문제 해결

		//메소드명 쿼리 메소드에도 @EntityGraph 적용 가능
		em.flush();
		em.clear();
		memberRepository.findEntityGraphByUsername("member1");

		//공통 인터페이스에 정의된 메소드도 오버라이딩하여 @EntityGraph 적용 가능
		em.flush();
		em.clear();
		memberRepository.findAll();
	}

	@Test
	public void queryHint() {
		//given
		Member member1 = memberRepository.save(new Member("member1", 10));
		em.flush();
		em.clear();

		//when
		Member findMember = memberRepository.findReadOnlyByUsername(member1.getUsername());
		findMember.setUsername("member2");

		// 변경감지가 안되기 때문에 쿼리가 나가지 않는다.
		em.flush();
	}

	@Test
	public void callCustom() {
		List<Member> result = memberRepository.findMemberCustom();
	}
}